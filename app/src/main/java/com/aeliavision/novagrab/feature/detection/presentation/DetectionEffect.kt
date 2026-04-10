package com.aeliavision.novagrab.feature.detection.presentation

sealed class DetectionEffect {
    data class EnqueueDownload(val videoUrl: String) : DetectionEffect()
}
