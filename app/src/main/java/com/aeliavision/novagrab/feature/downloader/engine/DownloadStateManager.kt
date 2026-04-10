package com.aeliavision.novagrab.feature.downloader.engine

import com.aeliavision.novagrab.feature.downloader.data.dao.DownloadTaskDao
import com.aeliavision.novagrab.feature.downloader.data.mapper.toDbString
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadStateManager @Inject constructor(
    private val downloadTaskDao: DownloadTaskDao,
) {
    suspend fun reconcileOnStartup() {
        downloadTaskDao.reconcileRunningToQueued()
    }

    suspend fun updateStatus(taskId: String, status: DownloadStatus) {
        val entity = downloadTaskDao.getById(taskId) ?: return
        downloadTaskDao.update(
            entity.copy(
                status = status.toDbString(),
                error = if (status is DownloadStatus.Failed) status.reason else null,
            )
        )
    }

    suspend fun updateProgress(taskId: String, status: DownloadStatus, downloadedBytes: Long) {
        downloadTaskDao.updateProgress(taskId, status.toDbString(), downloadedBytes)
    }

    suspend fun markCompleted(taskId: String, savedUri: String) {
        val entity = downloadTaskDao.getById(taskId) ?: return
        downloadTaskDao.update(
            entity.copy(
                status = DownloadStatus.Completed.toDbString(),
                savedUri = savedUri,
                completedAt = System.currentTimeMillis(),
                error = null,
            )
        )
    }
}
