package com.aeliavision.novagrab.feature.browser.presentation.tabs

sealed class BrowserTabsIntent {
    data object AddTab : BrowserTabsIntent()
    data class CloseTab(val tabId: String) : BrowserTabsIntent()
    data class SelectTab(val tabId: String) : BrowserTabsIntent()
    data object Back : BrowserTabsIntent()
}
