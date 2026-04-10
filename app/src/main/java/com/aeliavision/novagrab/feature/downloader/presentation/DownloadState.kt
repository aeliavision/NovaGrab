package com.aeliavision.novagrab.feature.downloader.presentation

import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask

data class DownloadState(
    val active: List<DownloadTask> = emptyList(),
    val totalStorageUsedBytes: Long = 0L,
    val averageSpeedBytesPerSec: Long = 0L,
    val speedBytesPerSecByTaskId: Map<String, Long> = emptyMap(),
)
