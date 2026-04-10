package com.aeliavision.novagrab.feature.player.presentation

import com.aeliavision.novagrab.core.common.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class PlayerIntent {
    data class Play(val uri: String) : PlayerIntent()
}

sealed class PlayerEffect

@HiltViewModel
class PlayerViewModel @Inject constructor() : MviViewModel<PlayerState, PlayerIntent, PlayerEffect>(
    initialState = PlayerState(),
) {
    override fun handleIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.Play -> updateState { copy(uri = intent.uri) }
        }
    }
}
