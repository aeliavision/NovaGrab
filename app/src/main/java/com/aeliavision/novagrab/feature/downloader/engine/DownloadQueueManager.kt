package com.aeliavision.novagrab.feature.downloader.engine

import com.aeliavision.novagrab.core.di.ApplicationScope
import com.aeliavision.novagrab.core.preferences.AppPreferences
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import com.aeliavision.novagrab.feature.downloader.repository.DownloadRepository
import com.aeliavision.novagrab.feature.downloader.scheduler.DownloadScheduler
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

@Singleton
class DownloadQueueManager @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadScheduler: DownloadScheduler,
    private val appPreferences: AppPreferences,
    @param:ApplicationScope private val appScope: CoroutineScope,
) {

    private val recentlyEnqueued = ConcurrentHashMap<String, Long>()
    private val promotionMutex = Mutex()

    init {
        appScope.launch {
            downloadRepository.getActiveTasksAsFlow()
                .distinctUntilChanged { old, new ->
                    old.map { it.id to it.status }.toSet() == new.map { it.id to it.status }.toSet()
                }
                .collect { activeTasks ->
                    try {
                        promoteQueuedTasks(activeTasks)
                    } catch (e: Exception) {
                        Timber.e(e, "DownloadQueueManager promotion failed")
                    }
                }
        }
    }

    private suspend fun promoteQueuedTasks(activeTasks: List<DownloadTask>) {
        promotionMutex.withLock {
            val maxConcurrent = appPreferences.maxConcurrentDownloads.first()
            val maxPerHost = appPreferences.maxConcurrentDownloadsPerHost.first()

            val running = activeTasks.filter { it.status == DownloadStatus.Running }
            if (running.size >= maxConcurrent) return

            val queued = downloadRepository.getQueuedTasks()
            val slotsAvailable = maxConcurrent - running.size

            val runningByHost = running.groupBy { it.hostName() }

            val now = System.currentTimeMillis()
            val windowMs = 60_000L
            recentlyEnqueued.entries.removeIf { (_, t) -> now - t > windowMs }

            queued.take(slotsAvailable).forEach { task ->
                val hostCount = runningByHost[task.hostName()]?.size ?: 0
                if (hostCount < maxPerHost && !recentlyEnqueued.containsKey(task.id)) {
                    recentlyEnqueued[task.id] = now
                    downloadScheduler.enqueue(task.id)
                }
            }
        }
    }

    private fun DownloadTask.hostName(): String =
        runCatching { android.net.Uri.parse(url).host ?: "unknown" }.getOrDefault("unknown")
}
