package com.aeliavision.novagrab.feature.browser.presentation

sealed class BrowserEffect {
    data class NavigateToUrl(val url: String) : BrowserEffect()
    data object GoBack : BrowserEffect()
    data object GoForward : BrowserEffect()
    data object NavigateToTabs : BrowserEffect()
    data object NavigateToBookmarks : BrowserEffect()
    data object NavigateToHistory : BrowserEffect()
    data object NavigateToDownloads : BrowserEffect()
    data object NavigateToSettings : BrowserEffect()
}
