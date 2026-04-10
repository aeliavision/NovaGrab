package com.aeliavision.novagrab.feature.browser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aeliavision.novagrab.feature.browser.data.entity.BrowserHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserHistoryDao {

    @Query("SELECT * FROM browser_history ORDER BY visited_at DESC")
    fun getHistoryAsFlow(): Flow<List<BrowserHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BrowserHistoryEntity)

    @Query("DELETE FROM browser_history")
    suspend fun clear()
}
