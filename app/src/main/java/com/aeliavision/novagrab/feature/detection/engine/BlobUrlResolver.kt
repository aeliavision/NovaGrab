package com.aeliavision.novagrab.feature.detection.engine

import javax.inject.Inject

class BlobUrlResolver @Inject constructor() {
    fun canResolve(url: String): Boolean = url.startsWith("blob:")
}
