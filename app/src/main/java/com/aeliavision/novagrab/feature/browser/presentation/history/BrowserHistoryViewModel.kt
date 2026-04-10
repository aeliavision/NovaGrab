package com.aeliavision.novagrab.feature.browser.presentation.history

import androidx.lifecycle.viewModelScope
import com.aeliavision.novagrab.core.common.MviViewModel
import com.aeliavision.novagrab.feature.browser.domain.repository.BrowserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class BrowserHistoryViewModel @Inject constructor(
    private val browserRepository: BrowserRepository,
) : MviViewModel<BrowserHistoryState, BrowserHistoryIntent, BrowserHistoryEffect>(
    initialState = BrowserHistoryState(),
) {

    init {
        browserRepository.getHistory()
            .onEach { items -> updateState { copy(items = items) } }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: BrowserHistoryIntent) {
        when (intent) {
            BrowserHistoryIntent.Back -> emitEffect(BrowserHistoryEffect.NavigateBack)

            is BrowserHistoryIntent.Open -> emitEffect(BrowserHistoryEffect.OpenUrl(intent.url))

            BrowserHistoryIntent.ClearAll -> {
                viewModelScope.launch {
                    browserRepository.clearHistory()
                }
            }
        }
    }
}
