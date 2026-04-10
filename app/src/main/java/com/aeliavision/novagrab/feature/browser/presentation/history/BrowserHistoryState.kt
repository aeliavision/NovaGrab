package com.aeliavision.novagrab.feature.browser.presentation.history

import com.aeliavision.novagrab.feature.browser.domain.model.BrowserHistoryItem

data class BrowserHistoryState(
    val items: List<BrowserHistoryItem> = emptyList(),
)
