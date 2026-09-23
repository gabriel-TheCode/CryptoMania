package com.thecode.cryptomania.presentation.feature.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.BuildConfig
import com.thecode.cryptomania.domain.model.ThemePreference
import com.thecode.cryptomania.domain.model.UserSettings
import com.thecode.cryptomania.domain.repository.CacheRepository
import com.thecode.cryptomania.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsIntent {
    data class ThemeSelected(val theme: ThemePreference) : SettingsIntent
    data class ColorBlindToggled(val enabled: Boolean) : SettingsIntent
    data object ClearCache : SettingsIntent
    data object ClearRecentSearches : SettingsIntent
}

sealed interface SettingsEffect {
    data object CacheCleared : SettingsEffect
}

@Immutable
data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val apiKeyConfigured: Boolean = false,
    val version: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val cacheRepository: CacheRepository,
) : ViewModel() {

    private val effects = Channel<SettingsEffect>(Channel.BUFFERED)
    val effect: Flow<SettingsEffect> = effects.receiveAsFlow()

    val state: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { SettingsUiState(it, BuildConfig.COINGECKO_API_KEY.isNotBlank(), BuildConfig.VERSION_NAME) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onIntent(intent: SettingsIntent) {
        viewModelScope.launch {
            when (intent) {
                is SettingsIntent.ThemeSelected -> settingsRepository.setTheme(intent.theme)
                is SettingsIntent.ColorBlindToggled -> settingsRepository.setColorBlindFriendly(intent.enabled)
                SettingsIntent.ClearCache -> {
                    cacheRepository.clearMarketCache()
                    effects.send(SettingsEffect.CacheCleared)
                }
                SettingsIntent.ClearRecentSearches -> settingsRepository.clearRecentSearches()
            }
        }
    }
}
