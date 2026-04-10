package com.aeliavision.novagrab.feature.detection.engine

import javax.inject.Inject

class HlsManifestDetector @Inject constructor() {

    fun looksLikeHls(url: String): Boolean {
        return url.contains(".m3u8", ignoreCase = true)
    }
}
