@file:OptIn(ExperimentalMaterial3Api::class)

package com.thecode.cryptomania.presentation.feature.coin

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thecode.cryptomania.R
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.CoinProfile
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.presentation.designsystem.component.BadgeStyle
import com.thecode.cryptomania.presentation.designsystem.component.CoinLogo
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaCard
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaTopBar
import com.thecode.cryptomania.presentation.designsystem.component.CryptoPrice
import com.thecode.cryptomania.presentation.designsystem.component.EmptyState
import com.thecode.cryptomania.presentation.designsystem.component.ErrorState
import com.thecode.cryptomania.presentation.designsystem.component.NetworkStatusIndicator
import com.thecode.cryptomania.presentation.designsystem.component.PriceChangeBadge
import com.thecode.cryptomania.presentation.designsystem.component.SectionHeader
import com.thecode.cryptomania.presentation.designsystem.component.SegmentedBar
import com.thecode.cryptomania.presentation.designsystem.component.SegmentedSelector
import com.thecode.cryptomania.presentation.designsystem.component.SkeletonBlock
import com.thecode.cryptomania.presentation.designsystem.component.accessibilityLabel
import com.thecode.cryptomania.presentation.designsystem.component.chart.PriceChart
import com.thecode.cryptomania.presentation.designsystem.component.contentColor
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.designsystem.theme.Motion
import com.thecode.cryptomania.presentation.util.Formatters
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.presentation.util.messageRes

@Composable
fun CoinDetailRoute(
    onBack: () -> Unit,
    viewModel: CoinDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(CoinDetailIntent.ScreenResumed)
        onPauseOrDispose { }
    }
    CoinDetailScreen(state = state, onIntent = viewModel::onIntent, onBack = onBack)
}

@Composable
fun CoinDetailScreen(
    state: CoinDetailUiState,
    onIntent: (CoinDetailIntent) -> Unit,
    onBack: () -> Unit,
) {
    val content = (state.content as? ScreenContent.Ready)?.data
    Column(Modifier.fillMaxSize()) {
        CryptoManiaTopBar(
            title = content?.name.orEmpty(),
            onBack = onBack,
            actions = {
                if (content != null) {
                    IconToggleButton(checked = state.isWatched, onCheckedChange = { onIntent(CoinDetailIntent.ToggleWatchlist) }) {
                        Icon(
                            imageVector = if (state.isWatched) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = stringResource(
                                if (state.isWatched) R.string.coin_remove_watchlist else R.string.coin_add_watchlist,
                            ),
                            tint = if (state.isWatched) CryptoManiaTheme.colors.warning else CryptoManiaTheme.colors.onWave,
                        )
                    }
                }
            },
        )
        NetworkStatusIndicator(state.syncStatus)
        when (val screen = state.content) {
            ScreenContent.Loading -> DetailSkeleton()
            is ScreenContent.Failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ErrorState(screen.error, onRetry = { onIntent(CoinDetailIntent.Retry) })
            }
            is ScreenContent.Ready -> DetailBody(screen.data, state, onIntent)
        }
    }
}

@Composable
private fun DetailBody(content: CoinDetailContent, state: CoinDetailUiState, onIntent: (CoinDetailIntent) -> Unit) {
    val spacing = CryptoManiaTheme.spacing
    // Scrubbing is high-frequency gesture feedback: kept local instead of round-tripping the ViewModel.
    var scrubbed by remember(state.range) { mutableStateOf<PricePoint?>(null) }
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val twoPane = maxWidth >= 840.dp
        val market: @Composable () -> Unit = {
            Column {
                Header(content, state, scrubbed)
                Spacer(Modifier.height(spacing.lg))
                ChartSection(state, onIntent, onScrub = { scrubbed = it })
                Spacer(Modifier.height(spacing.md))
                SegmentedSelector(
                    options = ChartRange.entries,
                    selected = state.range,
                    onSelect = { onIntent(CoinDetailIntent.RangeSelected(it)) },
                    label = { stringResource(it.shortLabel()) },
                    accessibilityLabel = { stringResource(R.string.cd_range, stringResource(it.longLabel())) },
                    modifier = Modifier.testTag("range_selector"),
                )
            }
        }
        val details: @Composable () -> Unit = {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.lg)) {
                Performance(content)
                DayRange(content)
                Stats(content)
                content.profile?.let { About(content.name, it) }
            }
        }
        if (twoPane) {
            Row(
                Modifier
                    .widthIn(max = 1200.dp)
                    .fillMaxSize()
                    .padding(horizontal = spacing.xxl),
                horizontalArrangement = Arrangement.spacedBy(spacing.xxl),
            ) {
                Column(Modifier.weight(1.3f).verticalScroll(rememberScrollState())) { market() }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    details()
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = spacing.lg),
            ) {
                Spacer(Modifier.height(spacing.sm))
                market()
                Spacer(Modifier.height(spacing.xxl))
                details()
                Spacer(Modifier.height(spacing.xxl))
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun Header(content: CoinDetailContent, state: CoinDetailUiState, scrubbed: PricePoint?) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val formatters = remember { Formatters() }
    val chart = (state.chart as? ChartUiState.Ready)?.chart
    Row(verticalAlignment = Alignment.CenterVertically) {
        CoinLogo(content.logoUrl, content.symbol, size = CryptoManiaTheme.sizes.logoLarge)
        Spacer(Modifier.size(spacing.md))
        Column {
            Text(content.symbol, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            Text(content.rank, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        }
    }
    Spacer(Modifier.height(spacing.md))
    if (scrubbed != null) {
        Text(formatters.price(scrubbed.price), style = MaterialTheme.typography.displayMedium, color = colors.textPrimary, maxLines = 1)
        Text(
            formatters.chartTimestamp(scrubbed.epochMillis, state.range),
            style = MaterialTheme.typography.labelLarge,
            color = colors.textSecondary,
        )
    } else {
        CryptoPrice(
            text = content.price,
            value = content.priceValue,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        // The change shown matches the selected chart range, so header and chart never disagree.
        val change = chart?.change ?: content.change24h
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            PriceChangeBadge(change)
            Text(
                stringResource(R.string.coin_range_past, stringResource((chart?.range ?: ChartRange.OneDay).longLabel())),
                style = MaterialTheme.typography.labelLarge,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun ChartSection(state: CoinDetailUiState, onIntent: (CoinDetailIntent) -> Unit, onScrub: (PricePoint?) -> Unit) {
    val height = CryptoManiaTheme.sizes.chartHeight
    AnimatedContent(
        targetState = state.chart,
        contentKey = { it::class },
        transitionSpec = { fadeIn(tween(Motion.MEDIUM)) togetherWith fadeOut(tween(Motion.SHORT)) },
        label = "chart",
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .testTag("price_chart"),
    ) { chartState ->
        when (chartState) {
            ChartUiState.Loading -> SkeletonBlock(Modifier.fillMaxSize(), height = height)
            ChartUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.chart_not_enough_data), color = CryptoManiaTheme.colors.textSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            is ChartUiState.Failed -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.AutoMirrored.Rounded.ShowChart,
                    title = stringResource(R.string.chart_unavailable),
                    message = stringResource(chartState.error.messageRes()),
                    action = { TextButton(onClick = { onIntent(CoinDetailIntent.Retry) }) { Text(stringResource(R.string.action_retry)) } },
                )
            }
            is ChartUiState.Ready -> {
                val chart = chartState.chart
                PriceChart(
                    points = chart.points,
                    lineColor = chart.trend.contentColor(),
                    highLabel = stringResource(R.string.chart_high, chart.high),
                    lowLabel = stringResource(R.string.chart_low, chart.low),
                    accessibilityDescription = stringResource(
                        R.string.cd_chart,
                        stringResource(chart.range.longLabel()),
                        chart.startPrice,
                        chart.endPrice,
                        chart.change.accessibilityLabel(),
                        chart.high,
                        chart.low,
                    ),
                    onScrub = onScrub,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun Performance(content: CoinDetailContent) {
    val spacing = CryptoManiaTheme.spacing
    Column {
        SectionHeader(stringResource(R.string.coin_performance))
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.sm)) {
            content.performance.forEach { (label, change) ->
                CryptoManiaCard(Modifier.weight(1f)) {
                    Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = CryptoManiaTheme.colors.textSecondary)
                    Spacer(Modifier.height(spacing.xs))
                    PriceChangeBadge(change, style = BadgeStyle.Plain, textStyle = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@Composable
private fun DayRange(content: CoinDetailContent) {
    val position = content.dayRangePosition ?: return
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    CryptoManiaCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.stat_range_24h), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        Spacer(Modifier.height(spacing.md))
        BoxWithConstraints(Modifier.fillMaxWidth().height(12.dp)) {
            SegmentedBar(
                segments = listOf(position to colors.brand.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().align(Alignment.Center),
            )
            Box(
                Modifier
                    .offset(x = (maxWidth - 12.dp) * position)
                    .size(12.dp)
                    .background(colors.brand, CircleShape),
            )
        }
        Spacer(Modifier.height(spacing.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabeledValue(stringResource(R.string.stat_low), content.low24h, Alignment.Start)
            LabeledValue(stringResource(R.string.stat_high), content.high24h, Alignment.End)
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String, alignment: Alignment.Horizontal) {
    Column(horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = CryptoManiaTheme.colors.textTertiary)
        Text(value, style = MaterialTheme.typography.titleSmall, color = CryptoManiaTheme.colors.textPrimary)
    }
}

@Composable
private fun Stats(content: CoinDetailContent) {
    val spacing = CryptoManiaTheme.spacing
    val colors = CryptoManiaTheme.colors
    Column {
        SectionHeader(stringResource(R.string.coin_stats))
        CryptoManiaCard(Modifier.fillMaxWidth()) {
            content.stats.chunked(2).forEachIndexed { index, pair ->
                if (index > 0) Spacer(Modifier.height(spacing.lg))
                Row(horizontalArrangement = Arrangement.spacedBy(spacing.lg)) {
                    pair.forEach { stat ->
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(stat.label), style = MaterialTheme.typography.labelMedium, color = colors.textSecondary, maxLines = 1)
                            Text(stat.value, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, maxLines = 1)
                            stat.progress?.let {
                                Spacer(Modifier.height(spacing.xs))
                                SegmentedBar(listOf(it to colors.brand), Modifier.fillMaxWidth(), height = 4.dp)
                            }
                            stat.detail?.let {
                                Text(it, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary, maxLines = 2)
                            }
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun About(name: String, profile: CoinProfile) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val uriHandler = LocalUriHandler.current
    var expanded by rememberSaveable { mutableStateOf(false) }
    if (profile.description.isBlank() && profile.homepageUrl == null && profile.categories.isEmpty()) return
    Column {
        SectionHeader(stringResource(R.string.coin_about, name))
        CryptoManiaCard(Modifier.fillMaxWidth()) {
            if (profile.description.isNotBlank()) {
                Text(
                    profile.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = if (expanded) Int.MAX_VALUE else 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.animateContentSize(),
                )
                TextButton(onClick = { expanded = !expanded }) {
                    Text(stringResource(if (expanded) R.string.coin_about_show_less else R.string.coin_about_show_more))
                }
            }
            if (profile.categories.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(spacing.xs), verticalArrangement = Arrangement.spacedBy(spacing.xs)) {
                    profile.categories.take(8).forEach { category ->
                        Text(
                            category,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                            modifier = Modifier
                                .background(colors.surfaceHigh, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    }
                }
                Spacer(Modifier.height(spacing.sm))
            }
            listOfNotNull(
                profile.genesisDate?.let { stringResource(R.string.coin_genesis, it) },
                profile.hashingAlgorithm?.let { stringResource(R.string.coin_algorithm, it) },
            ).forEach {
                Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
            }
            profile.homepageUrl?.let { url ->
                TextButton(onClick = { runCatching { uriHandler.openUri(url) } }) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(spacing.xs))
                    Text(stringResource(R.string.coin_website))
                }
            }
        }
    }
}

@Composable
private fun DetailSkeleton() {
    val spacing = CryptoManiaTheme.spacing
    Column(Modifier.padding(spacing.lg), verticalArrangement = Arrangement.spacedBy(spacing.md)) {
        SkeletonBlock(width = 120.dp, height = 20.dp)
        SkeletonBlock(width = 200.dp, height = 40.dp)
        SkeletonBlock(height = CryptoManiaTheme.sizes.chartHeight)
        SkeletonBlock(height = 44.dp)
    }
}

fun ChartRange.shortLabel(): Int = when (this) {
    ChartRange.OneDay -> R.string.range_1d
    ChartRange.OneWeek -> R.string.range_1w
    ChartRange.OneMonth -> R.string.range_1m
    ChartRange.ThreeMonths -> R.string.range_3m
    ChartRange.OneYear -> R.string.range_1y
}

fun ChartRange.longLabel(): Int = when (this) {
    ChartRange.OneDay -> R.string.range_1d_long
    ChartRange.OneWeek -> R.string.range_1w_long
    ChartRange.OneMonth -> R.string.range_1m_long
    ChartRange.ThreeMonths -> R.string.range_3m_long
    ChartRange.OneYear -> R.string.range_1y_long
}
