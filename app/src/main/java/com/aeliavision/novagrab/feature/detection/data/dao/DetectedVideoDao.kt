package com.aeliavision.novagrab.feature.detection.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeliavision.novagrab.feature.detection.data.entity.DetectedVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DetectedVideoDao {

    @Query("SELECT * FROM detected_videos WHERE tab_id = :tabId ORDER BY detected_at DESC")
    fun getByTabAsFlow(tabId: String): Flow<List<DetectedVideoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DetectedVideoEntity)

    @Query("DELETE FROM detected_videos WHERE tab_id = :tabId")
    suspend fun clearTab(tabId: String)
}
