package com.aeliavision.novagrab.feature.downloader.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.engine.DownloadStateManager
import com.aeliavision.novagrab.feature.downloader.notification.DownloadNotificationManager
import com.aeliavision.novagrab.feature.downloader.scheduler.DownloadScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DownloadForegroundService : Service() {

    @Inject lateinit var notificationManager: DownloadNotificationManager
    @Inject lateinit var downloadStateManager: DownloadStateManager
    @Inject lateinit var downloadScheduler: DownloadScheduler

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
                startForeground(NOTIFICATION_ID, notificationManager.buildForegroundNotification())
                notificationManager.showRunning(taskId)
                serviceScope.launch {
                    downloadScheduler.enqueue(taskId)
                    stopSelf(startId)
                }
            }

            ACTION_PAUSE_DOWNLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
                serviceScope.launch {
                    downloadStateManager.updateStatus(taskId, DownloadStatus.Paused)
                    downloadScheduler.cancel(taskId)
                    notificationManager.showPaused(taskId)
                    stopSelf(startId)
                }
            }

            ACTION_CANCEL_DOWNLOAD -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return START_NOT_STICKY
                serviceScope.launch {
                    downloadStateManager.updateStatus(taskId, DownloadStatus.Cancelled)
                    downloadScheduler.cancel(taskId)
                    notificationManager.cancel(taskId)
                    stopSelf(startId)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_DOWNLOAD = "action.START_DOWNLOAD"
        const val ACTION_PAUSE_DOWNLOAD = "action.PAUSE_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "action.CANCEL_DOWNLOAD"
        const val EXTRA_TASK_ID = "extra.TASK_ID"
        const val NOTIFICATION_ID = 1001
    }
}
