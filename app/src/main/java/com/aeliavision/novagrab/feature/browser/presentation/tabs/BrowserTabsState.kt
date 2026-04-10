package com.aeliavision.novagrab.feature.browser.presentation.tabs

data class BrowserTabsState(
    val tabs: List<BrowserTabItem> = emptyList(),
    val activeTabId: String? = null,
)

data class BrowserTabItem(
    val id: String,
    val title: String? = null,
    val url: String? = null,
)
