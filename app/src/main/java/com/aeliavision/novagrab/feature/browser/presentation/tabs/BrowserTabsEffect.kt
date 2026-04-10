package com.aeliavision.novagrab.feature.browser.presentation.tabs

sealed class BrowserTabsEffect {
    data object NavigateBack : BrowserTabsEffect()
}
