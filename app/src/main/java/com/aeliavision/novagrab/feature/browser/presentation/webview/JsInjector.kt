package com.aeliavision.novagrab.feature.browser.presentation.webview

import android.content.Context
import android.webkit.WebView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class JsInjector @Inject constructor(
    @param:ApplicationContext  private val context: Context,
) {

    private val scriptCache = mutableMapOf<String, String>()

    fun injectAll(webView: WebView) {
        injectAssetJs(webView, "js/blob_interceptor.js")
        injectAssetJs(webView, "js/video_detector.js")
        injectAssetJs(webView, "js/long_press_handler.js")
    }

    private fun injectAssetJs(webView: WebView, assetPath: String) {
        val script = scriptCache.getOrPut(assetPath) {
            context.assets.open(assetPath).bufferedReader().use { it.readText() }
        }

        webView.post {
            webView.evaluateJavascript(script, null)
        }
    }
}
