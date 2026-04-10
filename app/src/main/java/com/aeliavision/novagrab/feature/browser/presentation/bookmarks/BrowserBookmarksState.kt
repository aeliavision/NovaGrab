package com.aeliavision.novagrab.feature.browser.presentation.bookmarks

import com.aeliavision.novagrab.feature.browser.domain.model.BrowserBookmark

data class BrowserBookmarksState(
    val items: List<BrowserBookmark> = emptyList(),
)
