package com.aeliavision.novagrab.feature.downloader.repository

import androidx.paging.PagingSource
import com.aeliavision.novagrab.feature.downloader.data.entity.DownloadTaskEntity
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getActiveTasksAsFlow(): Flow<List<DownloadTask>>
    fun getCompletedTasksAsFlow(): Flow<List<DownloadTask>>
    fun getCompletedTaskEntitiesPaged(): PagingSource<Int, DownloadTaskEntity>
    suspend fun enqueue(task: DownloadTask)
    suspend fun getQueuedTasks(): List<DownloadTask>
    suspend fun getById(id: String): DownloadTask?
    suspend fun updateStatus(id: String, status: DownloadStatus)
    suspend fun updateProgress(id: String, status: DownloadStatus, downloadedBytes: Long)
    suspend fun markCompleted(id: String, savedUri: String)
    suspend fun delete(id: String)
}
