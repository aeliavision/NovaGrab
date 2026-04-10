package com.aeliavision.novagrab.feature.downloader.scheduler

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import com.aeliavision.novagrab.core.preferences.AppPreferences
import com.aeliavision.novagrab.feature.downloader.worker.DownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class DownloadScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences,
) {

    suspend fun enqueue(taskId: String) {
        val wifiOnly = appPreferences.wifiOnlyDownload.first()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "download_$taskId",
                ExistingWorkPolicy.KEEP,
                DownloadWorker.buildRequest(taskId, wifiOnly),
            )
    }

    fun cancel(taskId: String) {
        WorkManager.getInstance(context).cancelUniqueWork("download_$taskId")
    }
}
