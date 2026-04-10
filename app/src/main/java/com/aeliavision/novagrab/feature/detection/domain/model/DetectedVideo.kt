package com.aeliavision.novagrab.feature.detection.domain.model

import java.util.UUID

data class DetectedVideo(
    val id: String = UUID.randomUUID().toString(),
    val pageUrl: String,
    val videoUrl: String,
    val format: VideoFormat,
    val mimeType: String,
    val requestHeaders: Map<String, String> = emptyMap(),
    val estimatedSizeBytes: Long = -1L,
    val width: Int = 0,
    val height: Int = 0,
    val qualityVariants: List<com.aeliavision.novagrab.feature.downloader.domain.model.VideoQuality> = emptyList(),
    val detectedAt: Long = System.currentTimeMillis(),
    val tabId: String,
    val confidence: Int = 0,
    val title: String? = null,
    val thumbnailUrl: String? = null,
)
