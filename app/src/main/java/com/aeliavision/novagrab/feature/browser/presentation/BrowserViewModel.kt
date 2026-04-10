package com.aeliavision.novagrab.feature.browser.presentation

import androidx.lifecycle.viewModelScope
import com.aeliavision.novagrab.core.common.MviViewModel
import com.aeliavision.novagrab.feature.browser.domain.repository.BrowserRepository
import com.aeliavision.novagrab.feature.browser.domain.tabs.BrowserTabStore
import com.aeliavision.novagrab.feature.browser.presentation.webview.WebViewPool
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class BrowserViewModel @Inject constructor(
    val webViewPool: WebViewPool,
    private val tabStore: BrowserTabStore,
    private val browserRepository: BrowserRepository,
) : MviViewModel<BrowserState, BrowserIntent, BrowserEffect>(
    initialState = BrowserState.initial(),
) {

    init {
        tabStore.state
            .onEach { tabsState ->
                val active = tabsState.activeTab
                updateState {
                    copy(
                        tabId = active.id,
                        urlInput = active.url,
                        currentUrl = active.url,
                        pageTitle = active.title,
                        pageProgress = active.progress,
                        tabCount = tabsState.tabs.size,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: BrowserIntent) {
        when (intent) {
            is BrowserIntent.UrlInputChanged -> {
                updateState { copy(urlInput = intent.value) }
            }

            BrowserIntent.LoadFromInput -> {
                val raw = state.value.urlInput.trim()
                if (raw.isNotEmpty()) {
                    val normalized = normalizeUrl(raw)
                    tabStore.updateTabUrl(state.value.tabId, normalized)
                    updateState { copy(currentUrl = normalized) }
                    emitEffect(BrowserEffect.NavigateToUrl(normalized))
                }
            }

            is BrowserIntent.LoadUrl -> {
                val normalized = normalizeUrl(intent.url)
                tabStore.updateTabUrl(state.value.tabId, normalized)
                updateState { copy(urlInput = normalized, currentUrl = normalized) }
                emitEffect(BrowserEffect.NavigateToUrl(normalized))
            }

            BrowserIntent.GoBack -> emitEffect(BrowserEffect.GoBack)
            BrowserIntent.GoForward -> emitEffect(BrowserEffect.GoForward)

            BrowserIntent.ToggleDetectedSheet -> {
                updateState { copy(showDetectedSheet = !showDetectedSheet) }
            }

            BrowserIntent.OpenTabs -> emitEffect(BrowserEffect.NavigateToTabs)
            BrowserIntent.OpenBookmarks -> emitEffect(BrowserEffect.NavigateToBookmarks)
            BrowserIntent.OpenHistory -> emitEffect(BrowserEffect.NavigateToHistory)

            BrowserIntent.AddBookmark -> {
                val currentUrl = state.value.currentUrl
                if (currentUrl.isBlank()) return
                val title = webViewPool.getOrCreate(state.value.tabId).title
                viewModelScope.launch {
                    browserRepository.addBookmark(currentUrl, title)
                }
            }

            BrowserIntent.OpenDownloads -> emitEffect(BrowserEffect.NavigateToDownloads)
            BrowserIntent.OpenSettings -> emitEffect(BrowserEffect.NavigateToSettings)
        }
    }

    private fun normalizeUrl(raw: String): String {
        val input = raw.trim()
        if (input.isBlank()) return ""

        // Basic check: if it has a scheme, it's a URL
        if (input.startsWith("http://") || input.startsWith("https://") || input.startsWith("file://") || input.startsWith("about:")) {
            return input
        }

        // Regex for domain-like strings (e.g. example.com, test.co.uk)
        val domainPattern = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$".toRegex()
        
        return if (domainPattern.matches(input) || input == "localhost") {
            "https://$input"
        } else {
            // Treat as search query
            val encodedQuery = java.net.URLEncoder.encode(input, "UTF-8")
            "https://www.google.com/search?q=$encodedQuery"
        }
    }
}
