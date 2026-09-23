package com.thecode.cryptomania.presentation.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.component.CoinLogo
import com.thecode.cryptomania.presentation.designsystem.component.CryptoListItem
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaSearchBar
import com.thecode.cryptomania.presentation.designsystem.component.EmptyState
import com.thecode.cryptomania.presentation.designsystem.component.SectionHeader
import com.thecode.cryptomania.presentation.designsystem.component.WaveSurface
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.util.shortRes

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onOpenCoin: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openCoin by rememberUpdatedState(onOpenCoin)
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SearchEffect.OpenCoin -> openCoin(effect.coinId)
            }
        }
    }
    SearchScreen(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    state: SearchUiState,
    onIntent: (SearchIntent) -> Unit,
    onBack: () -> Unit,
) {
    val spacing = CryptoManiaTheme.spacing
    val colors = CryptoManiaTheme.colors
    val keyboard = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    // The field owns its text; the ViewModel only observes it.
    val field = rememberTextFieldState(state.query)
    LaunchedEffect(field) {
        snapshotFlow { field.text.toString() }.collect { onIntent(SearchIntent.QueryChanged(it)) }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val select: (String) -> Unit = { id ->
        keyboard?.hide()
        onIntent(SearchIntent.ResultSelected(id))
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
        WaveSurface {
        Row(
            Modifier
                .height(64.dp)
                .padding(start = spacing.xs, end = spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
            CryptoManiaSearchBar(
                state = field,
                placeholder = stringResource(R.string.market_search_hint),
                onSearch = {
                    keyboard?.hide()
                    onIntent(SearchIntent.Submit)
                },
                focusRequester = focusRequester,
                modifier = Modifier.testTag("search_field"),
            )
        }
        }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = CryptoManiaTheme.sizes.maxContentWidth)
                .testTag("search_results"),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = spacing.xxl),
        ) {
            if (state.query.isBlank()) {
                if (state.recentSearches.isNotEmpty()) {
                    item(key = "recent") {
                        Column(Modifier.padding(horizontal = spacing.lg)) {
                            SectionHeader(
                                title = stringResource(R.string.search_recent),
                                trailing = {
                                    TextButton(onClick = { onIntent(SearchIntent.ClearRecent) }) {
                                        Text(stringResource(R.string.action_clear))
                                    }
                                },
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.sm), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
                                state.recentSearches.forEach { recent ->
                                    RecentChip(recent, onClick = { field.setTextAndPlaceCursorAtEnd(recent) })
                                }
                            }
                            Spacer(Modifier.size(spacing.lg))
                        }
                    }
                }
                if (state.suggestions.isNotEmpty()) {
                    item(key = "top") { SectionHeader(stringResource(R.string.search_top_coins), Modifier.padding(horizontal = spacing.lg)) }
                    items(state.suggestions, key = { "s_" + it.id }) { coin ->
                        CryptoListItem(coin = coin, onClick = { select(coin.id) })
                    }
                }
            } else {
                items(state.localResults, key = { "l_" + it.id }) { coin ->
                    CryptoListItem(coin = coin, onClick = { select(coin.id) }, showRank = false)
                }
                when (val remote = state.remote) {
                    RemoteResults.Idle -> Unit
                    RemoteResults.Loading -> item(key = "remote_loading") {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(spacing.lg),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = colors.brand)
                        }
                    }
                    is RemoteResults.Loaded -> if (remote.hits.isNotEmpty()) {
                        item(key = "remote_header") {
                            SectionHeader(stringResource(R.string.search_more_results), Modifier.padding(horizontal = spacing.lg))
                        }
                        items(remote.hits, key = { "r_" + it.id }) { hit -> SearchHitRow(hit, onClick = { select(hit.id) }) }
                    }
                    is RemoteResults.Failed -> if (state.localResults.isNotEmpty()) {
                        item(key = "remote_failed") {
                            Text(
                                stringResource(R.string.search_remote_failed, stringResource(remote.error.shortRes())),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary,
                                modifier = Modifier.padding(spacing.lg),
                            )
                        }
                    }
                }
                if (state.showNoResults) {
                    item(key = "no_results") {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Rounded.SearchOff,
                                title = stringResource(R.string.search_no_results_title, state.query.trim()),
                                message = stringResource(R.string.search_no_results_message),
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun RecentChip(text: String, onClick: () -> Unit) {
    val colors = CryptoManiaTheme.colors
    Row(
        Modifier
            .heightIn(min = 36.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(Icons.Rounded.History, contentDescription = null, tint = colors.textTertiary, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
    }
}

@Composable
private fun SearchHitRow(hit: SearchHitUi, onClick: () -> Unit) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = CryptoManiaTheme.sizes.listItemMinHeight)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CoinLogo(hit.thumbUrl, hit.symbol)
        Spacer(Modifier.width(spacing.md))
        Column(Modifier.weight(1f)) {
            Text(hit.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(hit.symbol, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        }
        hit.rank?.let {
            Text(stringResource(R.string.search_rank, it), style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
        }
    }
}
