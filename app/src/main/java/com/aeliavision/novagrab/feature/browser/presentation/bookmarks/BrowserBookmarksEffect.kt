package com.aeliavision.novagrab.feature.browser.presentation.bookmarks

sealed class BrowserBookmarksEffect {
    data class OpenUrl(val url: String) : BrowserBookmarksEffect()
    data object NavigateBack : BrowserBookmarksEffect()
}
