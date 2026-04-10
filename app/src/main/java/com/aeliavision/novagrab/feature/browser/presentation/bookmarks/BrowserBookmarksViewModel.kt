package com.aeliavision.novagrab.feature.browser.presentation.bookmarks

import androidx.lifecycle.viewModelScope
import com.aeliavision.novagrab.core.common.MviViewModel
import com.aeliavision.novagrab.feature.browser.domain.repository.BrowserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class BrowserBookmarksViewModel @Inject constructor(
    private val browserRepository: BrowserRepository,
) : MviViewModel<BrowserBookmarksState, BrowserBookmarksIntent, BrowserBookmarksEffect>(
    initialState = BrowserBookmarksState(),
) {

    init {
        browserRepository.getBookmarks()
            .onEach { items -> updateState { copy(items = items) } }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: BrowserBookmarksIntent) {
        when (intent) {
            BrowserBookmarksIntent.Back -> emitEffect(BrowserBookmarksEffect.NavigateBack)

            is BrowserBookmarksIntent.Open -> {
                emitEffect(BrowserBookmarksEffect.OpenUrl(intent.url))
            }

            is BrowserBookmarksIntent.Remove -> {
                viewModelScope.launch {
                    browserRepository.removeBookmark(intent.id)
                }
            }
        }
    }
}
