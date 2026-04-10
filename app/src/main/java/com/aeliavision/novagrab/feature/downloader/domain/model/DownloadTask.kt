package com.aeliavision.novagrab.feature.downloader.domain.model

import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat
import java.util.UUID

data class DownloadTask(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val mimeType: String,
    val format: VideoFormat,
    val totalSizeBytes: Long = -1L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.Queued,
    val quality: VideoQuality? = null,
    val headers: Map<String, String> = emptyMap(),
    val sourcePageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val savedUri: String? = null,
    val error: String? = null,
    val retryCount: Int = 0,
)
