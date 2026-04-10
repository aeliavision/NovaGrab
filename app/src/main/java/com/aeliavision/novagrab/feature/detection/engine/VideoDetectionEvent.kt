package com.aeliavision.novagrab.feature.detection.engine

sealed class VideoDetectionEvent {
    abstract val tabId: String

    data class VideoFound(
        val url: String,
        val mimeType: String,
        val width: Int,
        val height: Int,
        override val tabId: String,
        val source: DetectionSource,
        val pageUrl: String = "",
        val capturedHeaders: Map<String, String> = emptyMap(),
        val title: String? = null,
    ) : VideoDetectionEvent()

    data class MseStreamDetected(
        val info: String,
        override val tabId: String,
    ) : VideoDetectionEvent()

    data class BlobUrlDetected(
        val url: String,
        val mimeType: String,
        val width: Int,
        val height: Int,
        override val tabId: String,
        val source: DetectionSource,
        val pageUrl: String = "",
    ) : VideoDetectionEvent()
}
