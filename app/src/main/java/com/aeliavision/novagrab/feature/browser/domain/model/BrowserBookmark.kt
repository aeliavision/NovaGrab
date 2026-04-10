package com.aeliavision.novagrab.feature.browser.domain.model

data class BrowserBookmark(
    val id: String,
    val url: String,
    val title: String?,
    val createdAt: Long,
)
