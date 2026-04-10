package com.aeliavision.novagrab.feature.browser.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "browser_history",
    indices = [Index(value = ["visited_at"])]
)
data class BrowserHistoryEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "visited_at") val visitedAt: Long,
)
