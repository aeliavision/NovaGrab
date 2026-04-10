package com.aeliavision.novagrab.feature.downloader.domain.model

data class DownloadProgress(
    val taskId: String,
    val downloadedBytes: Long,
    val totalBytes: Long,
    val percentage: Int,
    val speedBytesPerSec: Long = 0L,
)
