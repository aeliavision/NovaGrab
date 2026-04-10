package com.aeliavision.novagrab.feature.downloader.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat

@Entity(
    tableName = "download_tasks",
    indices = [
        Index(value = ["status"]),
        Index(value = ["created_at"]),
        Index(value = ["completed_at"])
    ]
)
data class DownloadTaskEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "format") val format: VideoFormat,
    @ColumnInfo(name = "total_size_bytes") val totalSizeBytes: Long,
    @ColumnInfo(name = "downloaded_bytes") val downloadedBytes: Long,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "headers") val headers: Map<String, String>,
    @ColumnInfo(name = "source_page_url") val sourcePageUrl: String?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
    @ColumnInfo(name = "saved_uri") val savedUri: String?,
    @ColumnInfo(name = "error") val error: String?,
    @ColumnInfo(name = "retry_count") val retryCount: Int,
)
