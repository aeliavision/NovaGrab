package com.aeliavision.novagrab.feature.browser.presentation

data class BrowserState(
    val tabId: String = "tab_0",
    val urlInput: String = "",
    val currentUrl: String = "",
    val pageTitle: String? = null,
    val tabCount: Int = 1,
    val pageProgress: Int = 100,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val showDetectedSheet: Boolean = false,
) {
    companion object {
        fun initial(): BrowserState = BrowserState()
    }
}
