@file:OptIn(ExperimentalMaterial3Api::class)

package com.thecode.cryptomania.presentation.feature.exchanges

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.component.CoinLogo
import com.thecode.cryptomania.presentation.designsystem.component.CoinRowSkeleton
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaPullToRefresh
import com.thecode.cryptomania.presentation.designsystem.component.CryptoManiaTopBar
import com.thecode.cryptomania.presentation.designsystem.component.EmptyState
import com.thecode.cryptomania.presentation.designsystem.component.ErrorState
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.util.Formatters
import com.thecode.cryptomania.presentation.util.ScreenContent

@Composable
fun ExchangesRoute(viewModel: ExchangesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.onIntent(ExchangesIntent.ScreenResumed)
        onPauseOrDispose { }
    }
    ExchangesScreen(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun ExchangesScreen(state: ExchangesUiState, onIntent: (ExchangesIntent) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = CryptoManiaTheme.colors.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CryptoManiaTopBar(
                scrollBehavior = scrollBehavior,
                title = stringResource(R.string.exchanges_title),
                subtitle = stringResource(R.string.exchanges_subtitle),
                status = state.syncStatus,
            )
        },
    ) { padding ->
        val top = padding.calculateTopPadding()
        when (val content = state.content) {
            ScreenContent.Loading -> Column(Modifier.padding(top = top)) { repeat(10) { CoinRowSkeleton() } }
            is ScreenContent.Failed -> Box(Modifier.fillMaxSize().padding(top = top), contentAlignment = Alignment.Center) {
                ErrorState(content.error, onRetry = { onIntent(ExchangesIntent.Refresh) })
            }
            is ScreenContent.Ready -> CryptoManiaPullToRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = { onIntent(ExchangesIntent.Refresh) },
                indicatorTopPadding = top,
                modifier = Modifier.fillMaxSize(),
            ) {
                if (content.data.exchanges.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(top = top), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Rounded.AccountBalance,
                            title = stringResource(R.string.exchanges_empty_title),
                            message = stringResource(R.string.exchanges_empty_message),
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        LazyColumn(
                            modifier = Modifier
                                .widthIn(max = CryptoManiaTheme.sizes.maxContentWidth)
                                .fillMaxSize()
                                .testTag("exchange_list"),
                            contentPadding = PaddingValues(top = top, bottom = CryptoManiaTheme.spacing.xxl),
                        ) {
                            items(content.data.exchanges, key = { it.id }, contentType = { "exchange" }) { exchange ->
                                ExchangeRow(
                                    exchange = exchange,
                                    onClick = exchange.websiteUrl?.let { url -> { runCatching { uriHandler.openUri(url) } } },
                                )
                                HorizontalDivider(
                                    Modifier.padding(start = 80.dp, end = CryptoManiaTheme.spacing.lg),
                                    color = CryptoManiaTheme.colors.divider,
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
private fun ExchangeRow(exchange: ExchangeUi, onClick: (() -> Unit)?) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val description = stringResource(
        R.string.cd_exchange_row,
        exchange.name,
        exchange.trustScore?.toString() ?: Formatters.PLACEHOLDER,
        exchange.volume,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = CryptoManiaTheme.sizes.listItemMinHeight)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .clearAndSetSemantics {
                contentDescription = description
                if (onClick != null) role = Role.Button
            }
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(exchange.rank, style = MaterialTheme.typography.labelMedium, color = colors.textTertiary, modifier = Modifier.width(30.dp))
        CoinLogo(exchange.logoUrl, exchange.name)
        Spacer(Modifier.width(spacing.md))
        Column(Modifier.weight(1f)) {
            Text(exchange.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (exchange.subtitle.isNotEmpty()) {
                Text(exchange.subtitle, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 96.dp)) {
            Text(
                stringResource(R.string.exchange_volume, exchange.volume),
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
            )
            exchange.trustScore?.let { TrustBadge(it) }
        }
    }
}

@Composable
private fun TrustBadge(score: Int) {
    val colors = CryptoManiaTheme.colors
    val (content: Color, container: Color) = when {
        score >= 8 -> colors.positive to colors.positiveContainer
        score >= 5 -> colors.warning to colors.warningContainer
        else -> colors.negative to colors.negativeContainer
    }
    Row(
        Modifier
            .padding(top = 2.dp)
            .background(container, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Rounded.VerifiedUser, contentDescription = null, tint = content, modifier = Modifier.size(12.dp))
        Text(stringResource(R.string.exchange_trust, score), style = MaterialTheme.typography.labelSmall, color = content)
    }
}
