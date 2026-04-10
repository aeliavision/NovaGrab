package com.aeliavision.novagrab.feature.browser.presentation.webview

import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.aeliavision.novagrab.feature.browser.domain.repository.BrowserRepository
import com.aeliavision.novagrab.feature.browser.domain.tabs.BrowserTabStore
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmartWebChromeClient(
    private val tabId: String,
    private val tabStore: BrowserTabStore,
    private val browserRepository: BrowserRepository,
    private val appScope: CoroutineScope,
) : WebChromeClient() {

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        tabStore.updateTabProgress(tabId, newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        val url = view?.url ?: return
        tabStore.updateTabTitle(tabId, title)
        tabStore.updateTabUrl(tabId, url)
        appScope.launch(Dispatchers.IO) {
            browserRepository.recordVisit(url = url, title = title)
        }
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return super.onConsoleMessage(consoleMessage)
    }

    class Factory @Inject constructor(
        private val tabStore: BrowserTabStore,
        private val browserRepository: BrowserRepository,
        @com.aeliavision.novagrab.core.di.ApplicationScope private val appScope: CoroutineScope,
    ) {
        fun create(tabId: String) = SmartWebChromeClient(tabId, tabStore, browserRepository, appScope)
    }
}
