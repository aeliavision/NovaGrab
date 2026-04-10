package com.aeliavision.novagrab.feature.browser.presentation.history

sealed class BrowserHistoryEffect {
    data class OpenUrl(val url: String) : BrowserHistoryEffect()
    data object NavigateBack : BrowserHistoryEffect()
}
