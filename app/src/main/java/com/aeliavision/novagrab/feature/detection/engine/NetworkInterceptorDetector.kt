package com.aeliavision.novagrab.feature.detection.engine

import javax.inject.Inject

class NetworkInterceptorDetector @Inject constructor(
    private val videoUrlFilter: VideoUrlFilter,
) {

    data class ScoredDetection(
        val scoredUrl: VideoUrlFilter.ScoredUrl,
        val capturedHeaders: Map<String, String>,
    )

    fun detect(url: String, headers: Map<String, String>): ScoredDetection? {
        val mime = headers.entries.firstOrNull { it.key.equals("content-type", ignoreCase = true) }?.value
            ?.substringBefore(';')
            ?.trim()

        val length = headers.entries.firstOrNull { it.key.equals("content-length", ignoreCase = true) }?.value
            ?.toLongOrNull() ?: -1L

        val scored = videoUrlFilter.score(url = url, mimeType = mime, contentLength = length) ?: return null

        val usefulHeaders = headers.filter { (key, _) ->
            key.equals("Referer", ignoreCase = true) ||
                key.equals("Origin", ignoreCase = true) ||
                key.equals("Cookie", ignoreCase = true) ||
                key.equals("Authorization", ignoreCase = true)
        }

        return ScoredDetection(scoredUrl = scored, capturedHeaders = usefulHeaders)
    }
}
