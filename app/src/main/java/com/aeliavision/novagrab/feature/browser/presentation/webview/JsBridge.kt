package com.aeliavision.novagrab.feature.browser.presentation.webview

import android.webkit.JavascriptInterface
import com.aeliavision.novagrab.core.di.ApplicationScope
import com.aeliavision.novagrab.feature.detection.engine.DetectionEngine
import com.aeliavision.novagrab.feature.detection.engine.VideoDetectionEvent
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JsBridge @Inject constructor(
    private val detectionEngine: DetectionEngine,
    @ApplicationScope private val scope: CoroutineScope,
) {

    fun forTab(tabId: String): JsBridgeInterface {
        return JsBridgeInterface(tabId)
    }

    inner class JsBridgeInterface(private val tabId: String) {

        @JavascriptInterface
        fun onVideoDetected(url: String, mimeType: String, width: Int, height: Int, title: String) {
            if (!isValidVideoUrl(url)) return
            scope.launch(Dispatchers.IO) {
                detectionEngine.onJsVideoDetected(
                    url = url,
                    mimeType = mimeType,
                    width = width,
                    height = height,
                    tabId = tabId,
                    title = title.ifBlank { null },
                )
            }
        }

        @JavascriptInterface
        fun onVideoLongPressed(url: String, width: Int, height: Int) {
            if (!isValidVideoUrl(url)) return
            scope.launch(Dispatchers.Main) {
                detectionEngine.triggerLongPressDownload(url, tabId)
            }
        }

        @JavascriptInterface
        fun onMseDetected(info: String) {
            scope.launch(Dispatchers.IO) {
                detectionEngine.submitDetection(
                    VideoDetectionEvent.MseStreamDetected(info, tabId)
                )
            }
        }

        @JavascriptInterface
        fun onMseSourceBuffer(mimeType: String) {
            scope.launch(Dispatchers.IO) {
                detectionEngine.onMseSourceBuffer(mimeType, tabId)
            }
        }

        @JavascriptInterface
        fun onEmbedDetected(embedUrl: String) {
            if (!isValidVideoUrl(embedUrl)) return
            scope.launch(Dispatchers.IO) {
                detectionEngine.analyzeEmbedUrl(embedUrl, tabId)
            }
        }

        @JavascriptInterface
        fun onBlobVideoDetected(url: String, mimeType: String, width: Int, height: Int) {
            if (!url.startsWith("blob:")) return
            scope.launch(Dispatchers.IO) {
                detectionEngine.onBlobUrlDetected(
                    url = url,
                    mimeType = mimeType,
                    width = width,
                    height = height,
                    tabId = tabId,
                )
            }
        }

        private fun isValidVideoUrl(url: String): Boolean {
            return url.startsWith("https://") || url.startsWith("http://")
        }
    }
}
