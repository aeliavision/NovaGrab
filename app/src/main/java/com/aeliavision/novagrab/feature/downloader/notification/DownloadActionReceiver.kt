package com.aeliavision.novagrab.feature.downloader.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.engine.DownloadStateManager
import com.aeliavision.novagrab.feature.downloader.scheduler.DownloadScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadActionReceiver : BroadcastReceiver() {

    @Inject lateinit var downloadStateManager: DownloadStateManager
    @Inject lateinit var downloadScheduler: DownloadScheduler
    @Inject lateinit var notificationManager: DownloadNotificationManager

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return

        when (intent.action) {
            ACTION_RESUME_DOWNLOAD -> {
                receiverScope.launch {
                    downloadStateManager.updateStatus(taskId, DownloadStatus.Queued)
                    notificationManager.showRunning(taskId)
                }
            }

            ACTION_PAUSE_DOWNLOAD -> {
                receiverScope.launch {
                    downloadStateManager.updateStatus(taskId, DownloadStatus.Paused)
                    downloadScheduler.cancel(taskId)
                    notificationManager.showPaused(taskId)
                }
            }

            ACTION_CANCEL_DOWNLOAD -> {
                receiverScope.launch {
                    downloadStateManager.updateStatus(taskId, DownloadStatus.Cancelled)
                    downloadScheduler.cancel(taskId)
                    notificationManager.cancel(taskId)
                }
            }
        }
    }

    companion object {
        const val ACTION_RESUME_DOWNLOAD = "action.RESUME_DOWNLOAD"
        const val ACTION_PAUSE_DOWNLOAD = "action.PAUSE_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "action.CANCEL_DOWNLOAD"
        const val EXTRA_TASK_ID = "extra.TASK_ID"
    }
}
