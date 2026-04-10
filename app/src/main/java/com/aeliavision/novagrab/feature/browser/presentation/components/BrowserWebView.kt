package com.aeliavision.novagrab.feature.browser.presentation.components


import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.aeliavision.novagrab.feature.browser.presentation.webview.WebViewPool

@Composable
fun BrowserWebView(
    tabId: String,
    webViewPool: WebViewPool,
    modifier: Modifier = Modifier,
    urlToLoad: String,
    goBackSignal: Int,
    goForwardSignal: Int,
) {
    val webView = remember(tabId) { webViewPool.getOrCreate(tabId) }

    AndroidView(
        modifier = modifier,
        factory = { webView },
    )

    LaunchedEffect(urlToLoad) {
        if (urlToLoad.isNotBlank() && webView.url != urlToLoad) {
            webView.loadUrl(urlToLoad)
        }
    }

    LaunchedEffect(goBackSignal) {
        if (goBackSignal > 0 && webView.canGoBack()) webView.goBack()
    }

    LaunchedEffect(goForwardSignal) {
        if (goForwardSignal > 0 && webView.canGoForward()) webView.goForward()
    }

    DisposableEffect(tabId) {
        onDispose {
            // WebView lifecycle managed by WebViewPool; do not destroy here.
        }
    }
}
