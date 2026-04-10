package com.aeliavision.novagrab.feature.downloader.domain.model

data class VideoQuality(
    val label: String,
    val bandwidth: Int,
    val resolution: String? = null,
)
