package com.aeliavision.novagrab.feature.browser.presentation.history

sealed class BrowserHistoryIntent {
    data class Open(val url: String) : BrowserHistoryIntent()
    data object ClearAll : BrowserHistoryIntent()
    data object Back : BrowserHistoryIntent()
}
