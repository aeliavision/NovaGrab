package com.aeliavision.novagrab.feature.downloader.repository

import android.net.Uri
import com.aeliavision.novagrab.core.storage.StorageManager
import com.aeliavision.novagrab.feature.downloader.data.dao.DownloadTaskDao
import com.aeliavision.novagrab.feature.downloader.data.entity.DownloadTaskEntity
import com.aeliavision.novagrab.feature.downloader.data.mapper.toDbString
import com.aeliavision.novagrab.feature.downloader.data.mapper.toDomain
import com.aeliavision.novagrab.feature.downloader.data.mapper.toEntity
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadTaskDao: DownloadTaskDao,
    private val storageManager: StorageManager,
) : DownloadRepository {

    override fun getActiveTasksAsFlow(): Flow<List<DownloadTask>> {
        return downloadTaskDao.getActiveTasksAsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCompletedTasksAsFlow(): Flow<List<DownloadTask>> {
        return downloadTaskDao.getCompletedTasksAsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getQueuedTasks(): List<DownloadTask> {
        return downloadTaskDao.getQueuedTasks().map { it.toDomain() }
    }

    override fun getCompletedTaskEntitiesPaged(): androidx.paging.PagingSource<Int, DownloadTaskEntity> {
        return downloadTaskDao.getCompletedTasksPaged()
    }

    override suspend fun enqueue(task: DownloadTask) {
        withContext(Dispatchers.IO) {
            downloadTaskDao.insert(task.toEntity())
        }
    }

    override suspend fun getById(id: String): DownloadTask? {
        return downloadTaskDao.getById(id)?.toDomain()
    }

    override suspend fun updateStatus(id: String, status: DownloadStatus) {
        val entity = downloadTaskDao.getById(id) ?: return
        downloadTaskDao.update(entity.copy(status = status.toDbString()))
    }

    override suspend fun updateProgress(id: String, status: DownloadStatus, downloadedBytes: Long) {
        downloadTaskDao.updateProgress(id, status.toDbString(), downloadedBytes)
    }

    override suspend fun markCompleted(id: String, savedUri: String) {
        val entity = downloadTaskDao.getById(id) ?: return
        downloadTaskDao.update(
            entity.copy(
                status = DownloadStatus.Completed.toDbString(),
                savedUri = savedUri,
                completedAt = System.currentTimeMillis(),
                error = null,
            )
        )
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            val task = downloadTaskDao.getById(id) ?: return@withContext
            task.savedUri?.let { uriString ->
                try {
                    val uri = Uri.parse(uriString)
                    storageManager.deleteFile(uri)
                } catch (e: Exception) {
                    // Ignore URI parsing errors
                }
            }
            downloadTaskDao.delete(id)
        }
    }

}
