package com.thecode.cryptomania

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.domain.model.ThemePreference
import com.thecode.cryptomania.domain.model.UserSettings
import com.thecode.cryptomania.domain.repository.SettingsRepository
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.feature.splash.AnimatedSplash
import com.thecode.cryptomania.presentation.navigation.CryptoManiaNavigation
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(settingsRepository: SettingsRepository) : ViewModel() {
    /** Null until preferences are read, so the first frame already uses the right theme. */
    val settings: StateFlow<UserSettings?> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // No artificial delay: the splash only stays until preferences are loaded (a few ms).
        splash.setKeepOnScreenCondition { viewModel.settings.value == null }
        // The system splash is plain brand blue, exactly like the first frame of AnimatedSplash:
        // drop it without its fade so the animation starts on screen, not behind it.
        var systemSplashGone by mutableStateOf(savedInstanceState != null)
        splash.setOnExitAnimationListener { provider ->
            provider.remove()
            systemSplashGone = true
        }
        enableEdgeToEdge()

        setContent {
            val loaded by viewModel.settings.collectAsStateWithLifecycle()
            // The splash stays up until preferences are read, so nothing is composed before that.
            val current = loaded ?: return@setContent
            val darkTheme = when (current.theme) {
                ThemePreference.System -> isSystemInDarkTheme()
                ThemePreference.Light -> false
                ThemePreference.Dark -> true
            }
            // Every screen draws the brand wave behind the status bar, so its icons are always light;
            // the navigation bar follows the in-app theme.
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { darkTheme },
                )
                onDispose {}
            }
            CryptoManiaTheme(darkTheme = darkTheme, colorBlindFriendly = current.colorBlindFriendly) {
                // Decided once: finishing onboarding navigates away rather than rebuilding the graph.
                val showOnboarding = remember { !current.onboardingCompleted }
                // Played once per launch (not on rotation), over the app while it loads.
                var splashDone by rememberSaveable { mutableStateOf(false) }
                Box {
                    CryptoManiaNavigation(showOnboarding = showOnboarding)
                    if (!splashDone) AnimatedSplash(onFinished = { splashDone = true }, ready = { systemSplashGone })
                }
            }
        }
    }

    private companion object {
        val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
        val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)
    }
}
