package com.aeliavision.novagrab.feature.settings.presentation


import androidx.lifecycle.viewModelScope
import com.aeliavision.novagrab.core.common.MviViewModel
import com.aeliavision.novagrab.core.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsState(
    val wifiOnly: Boolean = false,
    val downloadConnectTimeoutSeconds: Int = 30,
    val downloadReadTimeoutMinutes: Int = 60,
)

sealed class SettingsIntent {
    data class SetWifiOnly(val value: Boolean) : SettingsIntent()
    data class SetDownloadConnectTimeoutSeconds(val seconds: Int) : SettingsIntent()
    data class SetDownloadReadTimeoutMinutes(val minutes: Int) : SettingsIntent()
}

sealed class SettingsEffect

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
) : MviViewModel<SettingsState, SettingsIntent, SettingsEffect>(
    initialState = SettingsState(),
) {
    init {
        appPreferences.wifiOnlyDownload
            .onEach { enabled -> updateState { copy(wifiOnly = enabled) } }
            .launchIn(viewModelScope)

        appPreferences.downloadConnectTimeoutSeconds
            .onEach { seconds -> updateState { copy(downloadConnectTimeoutSeconds = seconds) } }
            .launchIn(viewModelScope)

        appPreferences.downloadReadTimeoutMinutes
            .onEach { minutes -> updateState { copy(downloadReadTimeoutMinutes = minutes) } }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.SetWifiOnly -> {
                viewModelScope.launch {
                    appPreferences.setWifiOnlyDownload(intent.value)
                }
            }

            is SettingsIntent.SetDownloadConnectTimeoutSeconds -> {
                viewModelScope.launch {
                    appPreferences.setDownloadConnectTimeoutSeconds(intent.seconds)
                }
            }

            is SettingsIntent.SetDownloadReadTimeoutMinutes -> {
                viewModelScope.launch {
                    appPreferences.setDownloadReadTimeoutMinutes(intent.minutes)
                }
            }
        }
    }
}
