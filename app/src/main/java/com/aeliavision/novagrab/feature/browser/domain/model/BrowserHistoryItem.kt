package com.aeliavision.novagrab.feature.browser.domain.model

data class BrowserHistoryItem(
    val id: String,
    val url: String,
    val title: String?,
    val visitedAt: Long,
)
