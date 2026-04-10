package com.aeliavision.novagrab.feature.downloader.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.aeliavision.novagrab.R
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadNotificationManager @Inject constructor(
    @param:ApplicationContext  private val context: Context,
) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW,
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun showRunning(taskId: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading")
            .setContentText("Starting…")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                0,
                "Pause",
                actionPendingIntent(taskId = taskId, action = DownloadActionReceiver.ACTION_PAUSE_DOWNLOAD),
            )
            .addAction(
                0,
                "Cancel",
                actionPendingIntent(taskId = taskId, action = DownloadActionReceiver.ACTION_CANCEL_DOWNLOAD),
            )
            .build()

        notificationManager.notify(taskId.hashCode(), n)
    }

    fun showPaused(taskId: String) {
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Download paused")
            .setContentText("Tap Resume to continue")
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .addAction(
                0,
                "Resume",
                actionPendingIntent(taskId = taskId, action = DownloadActionReceiver.ACTION_RESUME_DOWNLOAD),
            )
            .addAction(
                0,
                "Cancel",
                actionPendingIntent(taskId = taskId, action = DownloadActionReceiver.ACTION_CANCEL_DOWNLOAD),
            )
            .build()

        notificationManager.notify(taskId.hashCode(), n)
    }

    fun cancel(taskId: String) {
        notificationManager.cancel(taskId.hashCode())
    }

    fun updateProgress(taskId: String, progress: DownloadProgress) {
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Downloading")
            .setContentText("${progress.percentage}%")
            .setProgress(100, progress.percentage.coerceIn(0, 100), false)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .addAction(
                0,
                "Pause",
                actionPendingIntent(taskId = taskId, action = DownloadActionReceiver.ACTION_PAUSE_DOWNLOAD),
            )
            .addAction(
                0,
                "Cancel",
                actionPendingIntent(taskId = taskId, action = DownloadActionReceiver.ACTION_CANCEL_DOWNLOAD),
            )
            .build()

        notificationManager.notify(taskId.hashCode(), n)
    }

    companion object {
        private const val CHANNEL_ID = "downloads"
    }

    private fun actionPendingIntent(taskId: String, action: String): PendingIntent {
        val i = Intent(context, DownloadActionReceiver::class.java).apply {
            this.action = action
            putExtra(DownloadActionReceiver.EXTRA_TASK_ID, taskId)
        }

        val requestCode = (taskId + action).hashCode()
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
