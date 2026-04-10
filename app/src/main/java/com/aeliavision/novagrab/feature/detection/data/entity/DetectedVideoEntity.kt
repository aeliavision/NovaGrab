package com.aeliavision.novagrab.feature.detection.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat

@Entity(
    tableName = "detected_videos",
    indices = [
        Index(value = ["tab_id"]),
        Index(value = ["detected_at"])
    ]
)
data class DetectedVideoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "page_url") val pageUrl: String,
    @ColumnInfo(name = "video_url") val videoUrl: String,
    @ColumnInfo(name = "format") val format: VideoFormat,
    @ColumnInfo(name = "mime_type") val mimeType: String,
    @ColumnInfo(name = "request_headers") val requestHeaders: Map<String, String>,
    @ColumnInfo(name = "estimated_size_bytes") val estimatedSizeBytes: Long,
    @ColumnInfo(name = "width") val width: Int,
    @ColumnInfo(name = "height") val height: Int,
    @ColumnInfo(name = "detected_at") val detectedAt: Long,
    @ColumnInfo(name = "tab_id") val tabId: String,
    @ColumnInfo(name = "confidence") val confidence: Int,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String?,
)
