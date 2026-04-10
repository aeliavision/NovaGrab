package com.aeliavision.novagrab.feature.browser.presentation.tabs

import androidx.lifecycle.viewModelScope
import com.aeliavision.novagrab.core.common.MviViewModel
import com.aeliavision.novagrab.feature.browser.domain.tabs.BrowserTabStore
import com.aeliavision.novagrab.feature.browser.presentation.webview.WebViewPool
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class BrowserTabsViewModel @Inject constructor(
    private val webViewPool: WebViewPool,
    private val tabStore: BrowserTabStore,
) : MviViewModel<BrowserTabsState, BrowserTabsIntent, BrowserTabsEffect>(
    initialState = BrowserTabsState(),
) {

    init {
        tabStore.state
            .onEach { s ->
                updateState {
                    copy(
                        tabs = s.tabs.map { BrowserTabItem(id = it.id, title = it.title, url = it.url) },
                        activeTabId = s.activeTabId,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: BrowserTabsIntent) {
        when (intent) {
            BrowserTabsIntent.AddTab -> {
                tabStore.addTab()
                emitEffect(BrowserTabsEffect.NavigateBack)
            }

            is BrowserTabsIntent.CloseTab -> {
                webViewPool.destroy(intent.tabId)
                tabStore.closeTab(intent.tabId)
                emitEffect(BrowserTabsEffect.NavigateBack)
            }

            is BrowserTabsIntent.SelectTab -> {
                tabStore.selectTab(intent.tabId)
                emitEffect(BrowserTabsEffect.NavigateBack)
            }

            BrowserTabsIntent.Back -> emitEffect(BrowserTabsEffect.NavigateBack)
        }
    }
}
