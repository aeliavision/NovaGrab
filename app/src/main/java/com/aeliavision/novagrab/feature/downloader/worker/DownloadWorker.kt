package com.aeliavision.novagrab.feature.downloader.worker
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.engine.DownloadEngine
import com.aeliavision.novagrab.feature.downloader.engine.DownloadStateManager
import com.aeliavision.novagrab.feature.downloader.notification.DownloadNotificationManager
import com.aeliavision.novagrab.feature.downloader.repository.DownloadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import timber.log.Timber

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val downloadEngine: DownloadEngine,
    private val downloadRepository: DownloadRepository,
    private val downloadStateManager: DownloadStateManager,
    private val notificationManager: DownloadNotificationManager,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val notificationId = notificationIdForTask(taskId)

        try {
            setForeground(
                ForegroundInfo(
                    notificationId,
                    notificationManager.buildForegroundNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
                )
            )
        } catch (_: Throwable) {
            // If OS disallows starting FG service from background, WorkManager will still run.
        }

        return try {
            downloadStateManager.updateStatus(taskId, DownloadStatus.Running)
            var lastUpdateTimeMs = 0L
            downloadEngine.download(taskId).collect { progress ->
                val now = System.currentTimeMillis()
                val shouldUpdate = (now - lastUpdateTimeMs) >= 500L || progress.percentage >= 100
                if (shouldUpdate) {
                    lastUpdateTimeMs = now
                    downloadStateManager.updateProgress(taskId, DownloadStatus.Running, progress.downloadedBytes)
                    setProgress(
                        workDataOf(
                            KEY_PROGRESS to progress.percentage,
                            KEY_BYTES to progress.downloadedBytes,
                            KEY_SPEED to progress.speedBytesPerSec,
                        )
                    )
                    notificationManager.updateProgress(taskId, progress)
                }
            }

            Result.success()
        } catch (e: CancellationException) {
            val current = downloadRepository.getById(taskId)
            if (current?.status !is DownloadStatus.Cancelled && current?.status !is DownloadStatus.Paused) {
                downloadStateManager.updateStatus(taskId, DownloadStatus.Queued)
            }
            throw e
        } catch (e: Exception) {
            Timber.e(e, "DownloadWorker failed for taskId=$taskId")
            if (runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                downloadStateManager.updateStatus(
                    taskId,
                    DownloadStatus.Failed(e.message ?: "Unknown error"),
                )
                Result.failure()
            }
        }
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_PROGRESS = "progress"
        const val KEY_BYTES = "bytes"
        const val KEY_SPEED = "speed"
        const val MAX_RETRY_COUNT = 3
        const val WORKER_TAG = "download_worker"

        fun notificationIdForTask(taskId: String): Int =
            taskId.hashCode().and(0x7FFFFFFF) + 3000

        fun buildRequest(taskId: String, wifiOnly: Boolean = false): OneTimeWorkRequest {
            val networkType = if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
            return OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf(KEY_TASK_ID to taskId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(networkType)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS,
                )
                .addTag(WORKER_TAG)
                .addTag("task_$taskId")
                .build()
        }
    }
}
