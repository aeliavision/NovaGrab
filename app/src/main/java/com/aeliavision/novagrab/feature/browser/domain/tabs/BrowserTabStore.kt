package com.aeliavision.novagrab.feature.browser.domain.tabs

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

@Singleton
class BrowserTabStore @Inject constructor() {

    data class Tab(
        val id: String,
        val url: String,
        val title: String? = null,
        val progress: Int = 100,
    )

    data class State(
        val tabs: List<Tab> = listOf(Tab(id = "tab_0", url = "")),
        val activeTabId: String = "tab_0",
    ) {
        val activeTab: Tab get() = tabs.first { it.id == activeTabId }
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    fun addTab(url: String = ""): String {
        val id = "tab_${java.util.UUID.randomUUID()}"
        _state.update { it.copy(tabs = it.tabs + Tab(id = id, url = url, progress = 0), activeTabId = id) }
        return id
    }

    fun closeTab(id: String) {
        _state.update { current ->
            val remaining = current.tabs.filterNot { it.id == id }
            val newActive = if (current.activeTabId == id) {
                remaining.firstOrNull()?.id ?: ""
            } else {
                current.activeTabId
            }

            if (remaining.isEmpty()) {
                current.copy(tabs = listOf(Tab(id = "tab_0", url = "")), activeTabId = "tab_0")
            } else {
                current.copy(tabs = remaining, activeTabId = newActive)
            }
        }
    }

    fun selectTab(id: String) {
        _state.update { it.copy(activeTabId = id) }
    }

    fun updateTabUrl(id: String, url: String) {
        _state.update { current ->
            current.copy(tabs = current.tabs.map { if (it.id == id) it.copy(url = url, progress = 0) else it })
        }
    }

    fun updateTabTitle(id: String, title: String?) {
        _state.update { current ->
            current.copy(tabs = current.tabs.map { if (it.id == id) it.copy(title = title) else it })
        }
    }

    fun updateTabProgress(id: String, progress: Int) {
        _state.update { current ->
            current.copy(
                tabs = current.tabs.map {
                    if (it.id == id) it.copy(progress = progress.coerceIn(0, 100)) else it
                },
            )
        }
    }
}
