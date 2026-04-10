package com.aeliavision.novagrab.feature.browser.presentation.webview

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.aeliavision.novagrab.core.di.ApplicationScope
import com.aeliavision.novagrab.feature.detection.engine.DetectionEngine
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartWebViewClient(
    private val tabId: String,
    private val detectionEngine: DetectionEngine,
    private val jsInjector: JsInjector,
    private val appScope: CoroutineScope,
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val url = request.url

        return when (url.scheme?.lowercase()) {
            "http", "https" -> false
            else -> true
        }
    }

    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        val url = request.url.toString()

        appScope.launch(Dispatchers.IO) {
            detectionEngine.analyzeNetworkRequest(
                url = url,
                headers = request.requestHeaders,
                tabId = tabId,
            )
        }

        return null
    }

    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        detectionEngine.clearPageDetections(tabId)
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        jsInjector.injectAll(view)
    }

    class Factory @Inject constructor(
        private val detectionEngine: DetectionEngine,
        private val jsInjector: JsInjector,
        @ApplicationScope private val appScope: CoroutineScope,
    ) {
        fun create(tabId: String) = SmartWebViewClient(tabId, detectionEngine, jsInjector, appScope)
    }
}
