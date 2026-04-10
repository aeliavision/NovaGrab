package com.aeliavision.novagrab.feature.browser.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["created_at"])]
)
data class BookmarkEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
