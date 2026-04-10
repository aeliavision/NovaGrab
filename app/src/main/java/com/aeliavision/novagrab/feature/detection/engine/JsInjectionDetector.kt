package com.aeliavision.novagrab.feature.detection.engine

import javax.inject.Inject

class JsInjectionDetector @Inject constructor(
    private val videoUrlFilter: VideoUrlFilter,
) {

    fun detect(url: String, mimeType: String, width: Int, height: Int): VideoUrlFilter.ScoredUrl? {
        return videoUrlFilter.score(url = url, mimeType = mimeType, contentLength = -1L)
    }
}
