package com.aeliavision.novagrab

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.aeliavision.novagrab.core.storage.StorageManager
import com.aeliavision.novagrab.feature.downloader.engine.DownloadQueueManager
import com.aeliavision.novagrab.feature.downloader.engine.DownloadStateManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var downloadQueueManager: DownloadQueueManager
    @Inject lateinit var storageManager: StorageManager
    @Inject lateinit var downloadStateManager: DownloadStateManager

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        downloadQueueManager
        CoroutineScope(Dispatchers.IO).launch {
            downloadStateManager.reconcileOnStartup()
            storageManager.cleanupStaleTempDirs()
        }
    }
}
