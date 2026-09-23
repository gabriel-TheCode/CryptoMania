package com.thecode.cryptomania.presentation.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thecode.cryptomania.R
import com.thecode.cryptomania.domain.model.ThemePreference
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaCard
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaTopBar
import com.thecode.cryptomania.presentation.designsystem.component.SectionHeader
import com.thecode.cryptomania.presentation.designsystem.component.SegmentedSelector
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme

private const val COINGECKO_URL = "https://www.coingecko.com"
private const val SOURCE_URL = "https://github.com/gabriel-TheCode/CryptoMania"

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                SettingsEffect.CacheCleared -> snackbar.showSnackbar(resources.getString(R.string.settings_cache_cleared))
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        SettingsScreen(state = state, onIntent = viewModel::onIntent)
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun SettingsScreen(state: SettingsUiState, onIntent: (SettingsIntent) -> Unit) {
    val spacing = CryptoManiaTheme.spacing
    val colors = CryptoManiaTheme.colors
    val uriHandler = LocalUriHandler.current
    Column(Modifier.fillMaxSize()) {
        CryptoManiaTopBar(title = stringResource(R.string.settings_title))
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spacing.lg)
                .widthIn(max = CryptoManiaTheme.sizes.maxContentWidth)
                .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            SectionHeader(stringResource(R.string.settings_appearance))
            CryptoManiaCard(Modifier.fillMaxWidth()) {
                SegmentedSelector(
                    options = ThemePreference.entries,
                    selected = state.settings.theme,
                    onSelect = { onIntent(SettingsIntent.ThemeSelected(it)) },
                    label = {
                        stringResource(
                            when (it) {
                                ThemePreference.System -> R.string.settings_theme_system
                                ThemePreference.Light -> R.string.settings_theme_light
                                ThemePreference.Dark -> R.string.settings_theme_dark
                            },
                        )
                    },
                )
                Spacer(Modifier.height(spacing.md))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = state.settings.colorBlindFriendly,
                            role = Role.Switch,
                            onValueChange = { onIntent(SettingsIntent.ColorBlindToggled(it)) },
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_color_blind), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                        Text(stringResource(R.string.settings_color_blind_description), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    }
                    Switch(
                        checked = state.settings.colorBlindFriendly,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.brand),
                    )
                }
            }

            SectionHeader(stringResource(R.string.settings_data))
            CryptoManiaCard(Modifier.fillMaxWidth()) {
                ActionRow(
                    title = stringResource(R.string.settings_clear_cache),
                    description = stringResource(R.string.settings_clear_cache_description),
                    onClick = { onIntent(SettingsIntent.ClearCache) },
                )
                ActionRow(title = stringResource(R.string.settings_clear_recent), onClick = { onIntent(SettingsIntent.ClearRecentSearches) })
            }

            SectionHeader(stringResource(R.string.settings_api))
            CryptoManiaCard(Modifier.fillMaxWidth()) {
                Text(
                    stringResource(if (state.apiKeyConfigured) R.string.settings_api_key_configured else R.string.settings_api_keyless),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                )
                Text(stringResource(R.string.settings_api_description), style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }

            SectionHeader(stringResource(R.string.settings_about))
            CryptoManiaCard(Modifier.fillMaxWidth()) {
                ActionRow(title = stringResource(R.string.settings_attribution), external = true, onClick = { runCatching { uriHandler.openUri(COINGECKO_URL) } })
                ActionRow(title = stringResource(R.string.settings_source), external = true, onClick = { runCatching { uriHandler.openUri(SOURCE_URL) } })
                Text(
                    stringResource(R.string.settings_version, state.version),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = spacing.sm),
                )
            }
            Spacer(Modifier.height(spacing.xxl))
        }
    }
}

@Composable
private fun ActionRow(title: String, onClick: () -> Unit, description: String? = null, external: Boolean = false) {
    val colors = CryptoManiaTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = CryptoManiaTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
            if (description != null) Text(description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        }
        if (external) Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, tint = colors.textTertiary)
    }
}
