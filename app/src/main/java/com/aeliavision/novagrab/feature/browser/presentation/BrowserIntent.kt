package com.aeliavision.novagrab.feature.browser.presentation

sealed class BrowserIntent {
    data class UrlInputChanged(val value: String) : BrowserIntent()
    data object LoadFromInput : BrowserIntent()
    data class LoadUrl(val url: String) : BrowserIntent()
    data object GoBack : BrowserIntent()
    data object GoForward : BrowserIntent()
    data object ToggleDetectedSheet : BrowserIntent()
    data object OpenTabs : BrowserIntent()
    data object OpenBookmarks : BrowserIntent()
    data object OpenHistory : BrowserIntent()
    data object AddBookmark : BrowserIntent()
    data object OpenDownloads : BrowserIntent()
    data object OpenSettings : BrowserIntent()
}
