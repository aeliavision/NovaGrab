package com.aeliavision.novagrab.feature.downloader.engine

import android.net.Uri
import com.aeliavision.novagrab.core.storage.StorageManager
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadProgress
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.repository.DownloadRepository
import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class DownloadEngine @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val downloadStateManager: DownloadStateManager,
    private val httpRangeDownloader: HttpRangeDownloader,
    private val hlsDownloader: HlsDownloader,
    private val dashDownloader: DashDownloader,
    private val storageManager: StorageManager,
) {

    fun download(taskId: String): Flow<DownloadProgress> = channelFlow {
        val task = downloadRepository.getById(taskId) ?: return@channelFlow

        when (task.status) {
            is DownloadStatus.Cancelled -> return@channelFlow
            else -> Unit
        }

        suspend fun ensureNotPausedOrCancelled() {
            val current = downloadRepository.getById(taskId) ?: return
            if (current.status is DownloadStatus.Paused || current.status is DownloadStatus.Cancelled) {
                throw CancellationException("Task paused/cancelled")
            }
        }

        launch {
            while (isActive) {
                ensureNotPausedOrCancelled()
                delay(500L)
            }
        }

        val result: Result<Uri> = when (task.format) {
            is VideoFormat.HLS -> {
                hlsDownloader.download(task) {
                    ensureNotPausedOrCancelled()
                    trySend(it)
                }
            }

            is VideoFormat.DASH -> {
                dashDownloader.download(task) {
                    ensureNotPausedOrCancelled()
                    trySend(it)
                }
            }

            else -> {
                httpRangeDownloader.download(task) {
                    ensureNotPausedOrCancelled()
                    trySend(it)
                }
            }
        }

        val latest = downloadRepository.getById(taskId)
        if (latest?.status is DownloadStatus.Paused || latest?.status is DownloadStatus.Cancelled) {
            return@channelFlow
        }

        if (result.isSuccess) {
            val uri = result.getOrThrow()
            trySend(
                DownloadProgress(
                    taskId = taskId,
                    downloadedBytes = task.totalSizeBytes.coerceAtLeast(0),
                    totalBytes = task.totalSizeBytes,
                    percentage = 100,
                    speedBytesPerSec = 0,
                )
            )
            downloadStateManager.markCompleted(taskId, uri.toString())
            storageManager.deleteTempDirForTask(taskId)
        } else {
            downloadStateManager.updateStatus(
                taskId,
                DownloadStatus.Failed(result.exceptionOrNull()?.message ?: "Unknown error"),
            )
            storageManager.deleteTempDirForTask(taskId)
        }
    }
}
