package com.aeliavision.novagrab.feature.browser.presentation.webview

import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebViewPool @Inject constructor(
    @param:ApplicationContext  private val context: Context,
    private val jsBridge: JsBridge,
    private val webViewClientFactory: SmartWebViewClient.Factory,
    private val webViewChromeClientFactory: SmartWebChromeClient.Factory,
) {
    private val MAX_POOL_SIZE = 5

    private val pool = object : LinkedHashMap<String, WebView>(MAX_POOL_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, WebView>?): Boolean {
            return if (size > MAX_POOL_SIZE) {
                eldest?.value?.let { webView ->
                    webView.stopLoading()
                    webView.destroy()
                }
                true
            } else {
                false
            }
        }
    }

    @Synchronized
    fun getOrCreate(tabId: String): WebView {
        return pool.getOrPut(tabId) { createWebView(tabId) }
    }

    @Synchronized
    fun destroy(tabId: String) {
        pool[tabId]?.let { webView ->
            webView.stopLoading()
            webView.destroy()
            pool.remove(tabId)
        }
    }

    private fun createWebView(tabId: String): WebView {
        return WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                mediaPlaybackRequiresUserGesture = false
                mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                userAgentString = SmartUserAgent.CHROME_LATEST

                allowUniversalAccessFromFileURLs = false
                allowFileAccessFromFileURLs = false
                allowContentAccess = false
            }
            addJavascriptInterface(jsBridge.forTab(tabId), "SmartVDL")
            webViewClient = webViewClientFactory.create(tabId)
            webChromeClient = webViewChromeClientFactory.create(tabId)
        }
    }
}
