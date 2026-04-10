package com.aeliavision.novagrab.feature.downloader.presentation

import android.os.SystemClock
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.aeliavision.novagrab.core.common.MviViewModel
import com.aeliavision.novagrab.feature.downloader.data.mapper.toDomain
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import com.aeliavision.novagrab.feature.downloader.engine.DownloadStateManager
import com.aeliavision.novagrab.feature.downloader.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadStateManager: DownloadStateManager,
    private val downloadScheduler: com.aeliavision.novagrab.feature.downloader.scheduler.DownloadScheduler,
) : MviViewModel<DownloadState, DownloadIntent, DownloadEffect>(
    initialState = DownloadState(),
) {

    val historyPaging: Flow<PagingData<DownloadTask>> = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { downloadRepository.getCompletedTaskEntitiesPaged() },
    ).flow
        .map { pagingData -> pagingData.map { entity -> entity.toDomain() } }
        .cachedIn(viewModelScope)

    private data class SpeedSample(
        val bytes: Long,
        val timeMs: Long,
        val speedBytesPerSec: Long,
    )

    private val speedSamplesByTaskId: MutableMap<String, SpeedSample> = mutableMapOf()

    init {
        downloadRepository.getActiveTasksAsFlow()
            .debounce(300)
            .onEach { list ->
                val nowMs = SystemClock.elapsedRealtime()
                val speedById = mutableMapOf<String, Long>()

                list.forEach { task ->
                    val downloaded = task.downloadedBytes
                    val prev = speedSamplesByTaskId[task.id]

                    val speed = if (prev == null) {
                        0L
                    } else {
                        val deltaBytes = (downloaded - prev.bytes).coerceAtLeast(0)
                        val deltaMs = (nowMs - prev.timeMs).coerceAtLeast(1)
                        ((deltaBytes * 1000L) / deltaMs).coerceAtLeast(0L)
                    }

                    val persistedSpeed = if (speed > 0) speed else (prev?.speedBytesPerSec ?: 0L)
                    speedSamplesByTaskId[task.id] = SpeedSample(
                        bytes = downloaded,
                        timeMs = nowMs,
                        speedBytesPerSec = persistedSpeed,
                    )
                    speedById[task.id] = persistedSpeed
                }

                // Avoid unbounded growth when tasks disappear
                val activeIds = list.mapTo(mutableSetOf()) { it.id }
                speedSamplesByTaskId.keys.retainAll(activeIds)

                val runningSpeeds = list
                    .filter { it.status == DownloadStatus.Running }
                    .mapNotNull { speedById[it.id] }
                    .filter { it > 0 }

                val avgSpeed = if (runningSpeeds.isNotEmpty()) {
                    runningSpeeds.sum() / runningSpeeds.size
                } else {
                    0L
                }

                updateState {
                    copy(
                        active = list,
                        speedBytesPerSecByTaskId = speedById,
                        averageSpeedBytesPerSec = avgSpeed,
                    )
                }
            }
            .launchIn(viewModelScope)

        downloadRepository.getCompletedTasksAsFlow()
            .onEach { completed ->
                // Prefer totalSizeBytes when present; fall back to downloadedBytes.
                val sum = completed.sumOf { task ->
                    when {
                        task.totalSizeBytes > 0 -> task.totalSizeBytes
                        task.downloadedBytes > 0 -> task.downloadedBytes
                        else -> 0L
                    }
                }
                updateState { copy(totalStorageUsedBytes = sum) }
            }
            .launchIn(viewModelScope)

        // historyPaging is exposed as a stable property (not part of DownloadState)
    }

    override fun handleIntent(intent: DownloadIntent) {
        when (intent) {
            is DownloadIntent.Pause -> {
                viewModelScope.launch {
                    downloadStateManager.updateStatus(intent.taskId, DownloadStatus.Paused)
                    downloadScheduler.cancel(intent.taskId)
                }
            }

            is DownloadIntent.Resume -> {
                viewModelScope.launch {
                    downloadStateManager.updateStatus(intent.taskId, DownloadStatus.Queued)
                }
            }

            is DownloadIntent.Cancel -> {
                viewModelScope.launch {
                    downloadStateManager.updateStatus(intent.taskId, DownloadStatus.Cancelled)
                    downloadScheduler.cancel(intent.taskId)
                }
            }

            is DownloadIntent.OpenPlayer -> {
                viewModelScope.launch {
                    val task = downloadRepository.getById(intent.taskId) ?: return@launch
                    val uri = task.savedUri ?: return@launch
                    emitEffect(DownloadEffect.OpenPlayer(uri))
                }
            }

            is DownloadIntent.Share -> {
                viewModelScope.launch {
                    val task = downloadRepository.getById(intent.taskId) ?: return@launch
                    val uri = task.savedUri ?: return@launch
                    emitEffect(DownloadEffect.ShareDownload(uri, task.mimeType))
                }
            }

            is DownloadIntent.Delete -> {
                viewModelScope.launch {
                    downloadRepository.delete(intent.taskId)
                }
            }
        }
    }

}
