package com.aeliavision.novagrab.feature.browser.presentation.bookmarks

sealed class BrowserBookmarksIntent {
    data class Open(val url: String) : BrowserBookmarksIntent()
    data class Remove(val id: String) : BrowserBookmarksIntent()
    data object Back : BrowserBookmarksIntent()
}
