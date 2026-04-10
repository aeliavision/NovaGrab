package com.aeliavision.novagrab.feature.downloader.data.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aeliavision.novagrab.feature.downloader.data.entity.DownloadTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadTaskDao {

    @Query("SELECT * FROM download_tasks ORDER BY created_at DESC")
    fun getAllTasksAsFlow(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status IN ('QUEUED', 'RUNNING', 'PAUSED') ORDER BY created_at ASC")
    fun getActiveTasksAsFlow(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE id = :id")
    suspend fun getById(id: String): DownloadTaskEntity?

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' ORDER BY completed_at DESC")
    fun getCompletedTasksAsFlow(): Flow<List<DownloadTaskEntity>>

    @Query("SELECT * FROM download_tasks WHERE status = 'COMPLETED' ORDER BY completed_at DESC")
    fun getCompletedTasksPaged(): PagingSource<Int, DownloadTaskEntity>

    @Query("SELECT * FROM download_tasks WHERE status = 'QUEUED' ORDER BY created_at ASC")
    suspend fun getQueuedTasks(): List<DownloadTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: DownloadTaskEntity)

    @Update
    suspend fun update(task: DownloadTaskEntity)

    @Query("UPDATE download_tasks SET status = :status, downloaded_bytes = :downloadedBytes WHERE id = :id")
    suspend fun updateProgress(id: String, status: String, downloadedBytes: Long)

    @Query("UPDATE download_tasks SET status = 'CANCELLED' WHERE id = :id")
    suspend fun cancel(id: String)

    @Query("DELETE FROM download_tasks WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM download_tasks WHERE status = 'COMPLETED'")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM download_tasks WHERE status IN ('QUEUED', 'RUNNING')")
    fun getActiveCountAsFlow(): Flow<Int>

    @Query("UPDATE download_tasks SET status = 'QUEUED' WHERE status = 'RUNNING'")
    suspend fun reconcileRunningToQueued()
}
