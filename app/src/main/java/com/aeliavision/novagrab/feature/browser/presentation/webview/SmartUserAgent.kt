package com.aeliavision.novagrab.feature.browser.presentation.webview

object SmartUserAgent {
    val CHROME_LATEST: String = System.getProperty("http.agent") ?: ""
}
