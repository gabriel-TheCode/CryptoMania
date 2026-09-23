@file:OptIn(ExperimentalMaterial3Api::class)

package com.thecode.cryptomania.presentation.feature.market

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thecode.cryptomania.R
import com.thecode.cryptomania.domain.usecase.MarketFilter
import com.thecode.cryptomania.presentation.designsystem.component.CoinRowSkeleton
import com.thecode.cryptomania.presentation.designsystem.component.CryptoListItem
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaCard
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaFilterChip
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaPullToRefresh
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaTopBar
import com.thecode.cryptomania.presentation.designsystem.component.EmptyState
import com.thecode.cryptomania.presentation.designsystem.component.ErrorState
import com.thecode.cryptomania.presentation.designsystem.component.MarketStat
import com.thecode.cryptomania.presentation.designsystem.component.MoverCard
import com.thecode.cryptomania.presentation.designsystem.component.NetworkStatusIndicator
import com.thecode.cryptomania.presentation.designsystem.component.PriceChangeBadge
import com.thecode.cryptomania.presentation.designsystem.component.SectionHeader
import com.thecode.cryptomania.presentation.designsystem.component.SegmentedBar
import com.thecode.cryptomania.presentation.designsystem.component.SkeletonBlock
import com.thecode.cryptomania.presentation.designsystem.component.staggeredEntrance
import kotlinx.coroutines.delay
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.presentation.util.rememberRelativeTime

@Composable
fun MarketRoute(
    onOpenCoin: (String) -> Unit,
    onOpenSearch: () -> Unit,
    viewModel: MarketViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(MarketIntent.ScreenResumed)
        onPauseOrDispose { }
    }
    MarketScreen(state = state, onIntent = viewModel::onIntent, onOpenCoin = onOpenCoin, onOpenSearch = onOpenSearch)
}

@Composable
fun MarketScreen(
    state: MarketUiState,
    onIntent: (MarketIntent) -> Unit,
    onOpenCoin: (String) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val content = state.content
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
    ) {
        CryptoManiaTopBar(
            scrollBehavior = scrollBehavior,
            title = stringResource(R.string.app_name),
            subtitle = (content as? ScreenContent.Ready)?.data?.fetchedAt?.let {
                stringResource(R.string.status_updated, rememberRelativeTime(it))
            },
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.action_search))
                }
            },
        )
        NetworkStatusIndicator(state.syncStatus)
        when (content) {
            ScreenContent.Loading -> MarketSkeleton()
            is ScreenContent.Failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorState(content.error, onRetry = { onIntent(MarketIntent.Refresh) })
            }
            is ScreenContent.Ready -> CryptoManiaPullToRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(MarketIntent.Refresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                MarketList(
                    content = content.data,
                    filter = state.filter,
                    onFilterSelected = { onIntent(MarketIntent.FilterSelected(it)) },
                    onOpenCoin = onOpenCoin,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarketList(
    content: MarketContent,
    filter: MarketFilter,
    onFilterSelected: (MarketFilter) -> Unit,
    onOpenCoin: (String) -> Unit,
) {
    val spacing = CryptoManiaTheme.spacing
    val colors = CryptoManiaTheme.colors
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val expanded = maxWidth >= 720.dp
        val listState = rememberLazyListState()
        // Entrance animation plays once per screen visit, on the first rows only.
        val animateEntrance = rememberSaveable { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            delay(800)
            animateEntrance.value = false
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .widthIn(max = 1100.dp)
                .fillMaxSize()
                .testTag("market_list"),
            contentPadding = PaddingValues(bottom = spacing.xxl),
        ) {
            item(key = "overview", contentType = "overview") {
                if (content.global != null) {
                    GlobalOverview(
                        global = content.global,
                        modifier = Modifier.padding(horizontal = spacing.lg, vertical = spacing.sm),
                    )
                }
            }
            if (content.movers.isNotEmpty()) {
                item(key = "movers", contentType = "movers") {
                    Column {
                        SectionHeader(
                            title = stringResource(R.string.market_movers),
                            modifier = Modifier.padding(horizontal = spacing.lg),
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = spacing.lg),
                            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
                        ) {
                            items(content.movers, key = { it.id }, contentType = { "mover" }) { coin ->
                                MoverCard(coin = coin, onClick = { onOpenCoin(coin.id) })
                            }
                        }
                        Spacer(Modifier.height(spacing.lg))
                    }
                }
            }
            stickyHeader(key = "filters", contentType = "filters") {
                FilterHeader(filter = filter, onFilterSelected = onFilterSelected, expanded = expanded)
            }
            if (content.coins.isEmpty()) {
                item(key = "empty", contentType = "empty") {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        if (filter == MarketFilter.Watchlist) {
                            EmptyState(
                                icon = Icons.Rounded.StarBorder,
                                title = stringResource(R.string.watchlist_empty_title),
                                message = stringResource(R.string.watchlist_empty_message),
                            )
                        } else {
                            EmptyState(
                                icon = Icons.Rounded.Tune,
                                title = stringResource(R.string.filter_empty_title),
                                message = stringResource(R.string.filter_empty_message),
                            )
                        }
                    }
                }
            }
            itemsIndexed(content.coins, key = { _, coin -> coin.id }, contentType = { _, _ -> "coin" }) { index, coin ->
                CryptoListItem(
                    coin = coin,
                    onClick = { onOpenCoin(coin.id) },
                    expanded = expanded,
                    modifier = Modifier
                        .animateItem()
                        .staggeredEntrance(index, animateEntrance.value),
                )
                HorizontalDivider(Modifier.padding(start = 80.dp, end = spacing.lg), color = colors.divider)
            }
        }
    }
}

@Composable
private fun GlobalOverview(global: GlobalMarketUi, modifier: Modifier = Modifier) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val ethColor = colors.textSecondary
    CryptoManiaCard(modifier = modifier.fillMaxWidth()) {
        Text(stringResource(R.string.market_cap_global), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        Spacer(Modifier.height(spacing.xs))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            Text(global.totalMarketCap, style = MaterialTheme.typography.displaySmall, color = colors.textPrimary)
            PriceChangeBadge(global.change24h)
        }
        Spacer(Modifier.height(spacing.lg))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.xxl)) {
            MarketStat(label = stringResource(R.string.market_volume_global), value = global.volume24h)
            MarketStat(label = stringResource(R.string.market_dominance), value = stringResource(R.string.market_dominance_btc, global.btcDominanceText))
        }
        Spacer(Modifier.height(spacing.md))
        SegmentedBar(
            segments = listOf(global.btcDominance to colors.brand, global.ethDominance to ethColor),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
            Legend(colors.brand, stringResource(R.string.market_dominance_btc, global.btcDominanceText))
            Legend(ethColor, stringResource(R.string.market_dominance_eth, global.ethDominanceText))
            Legend(colors.surfaceHigh, stringResource(R.string.market_dominance_other, global.otherDominanceText))
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Text(label, style = MaterialTheme.typography.labelSmall, color = CryptoManiaTheme.colors.textSecondary)
    }
}

@Composable
private fun FilterHeader(filter: MarketFilter, onFilterSelected: (MarketFilter) -> Unit, expanded: Boolean) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    Column(Modifier.fillMaxWidth().background(colors.background)) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
        ) {
            items(MarketFilter.entries, key = { it.name }) { option ->
                CryptoManiaFilterChip(
                    label = stringResource(option.labelRes()),
                    selected = option == filter,
                    onClick = { onFilterSelected(option) },
                    icon = if (option == MarketFilter.Watchlist) Icons.Rounded.Star else null,
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ColumnLabel(stringResource(R.string.market_column_asset), Modifier.weight(1f), TextAlign.Start)
            if (expanded) {
                ColumnLabel(stringResource(R.string.market_column_market_cap), Modifier.width(112.dp), TextAlign.End)
                ColumnLabel(stringResource(R.string.market_column_volume), Modifier.width(112.dp), TextAlign.End)
            }
            ColumnLabel(stringResource(R.string.market_column_7d), Modifier.width(CryptoManiaTheme.sizes.sparklineWidth + spacing.md * 2), TextAlign.Center)
            ColumnLabel(stringResource(R.string.market_column_price), Modifier.widthIn(min = 92.dp), TextAlign.End)
        }
        HorizontalDivider(color = colors.divider)
    }
}

@Composable
private fun ColumnLabel(text: String, modifier: Modifier, align: TextAlign) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = CryptoManiaTheme.colors.textTertiary, textAlign = align, modifier = modifier, maxLines = 1)
}

@Composable
private fun MarketSkeleton() {
    val spacing = CryptoManiaTheme.spacing
    Column(Modifier.fillMaxSize().padding(top = spacing.sm)) {
        Column(Modifier.padding(horizontal = spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            SkeletonBlock(width = 120.dp, height = 12.dp)
            SkeletonBlock(width = 220.dp, height = 32.dp)
            SkeletonBlock(height = 6.dp)
        }
        Spacer(Modifier.height(spacing.xxl))
        repeat(8) { CoinRowSkeleton() }
    }
}

private fun MarketFilter.labelRes(): Int = when (this) {
    MarketFilter.All -> R.string.filter_all
    MarketFilter.Watchlist -> R.string.filter_watchlist
    MarketFilter.Gainers -> R.string.filter_gainers
    MarketFilter.Losers -> R.string.filter_losers
}
