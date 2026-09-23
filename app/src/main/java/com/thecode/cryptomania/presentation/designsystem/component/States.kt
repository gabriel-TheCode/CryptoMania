package com.thecode.cryptomania.presentation.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.util.SyncStatus
import com.thecode.cryptomania.presentation.util.messageRes
import com.thecode.cryptomania.presentation.util.rememberRelativeTime
import com.thecode.cryptomania.presentation.util.shortRes
import com.thecode.cryptomania.presentation.util.titleRes

/** Shimmering placeholder block. */
@Composable
fun SkeletonBlock(modifier: Modifier = Modifier, width: Dp? = null, height: Dp = 14.dp) {
    val colors = CryptoManiaTheme.colors
    val transition = rememberInfiniteTransition(label = "skeleton")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1_100, easing = LinearEasing), RepeatMode.Restart),
        label = "skeletonProgress",
    )
    Box(
        modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(MaterialTheme.shapes.extraSmall)
            .drawWithContent {
                val x = size.width * (progress * 2f - 0.5f)
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(colors.skeleton, colors.skeletonHighlight, colors.skeleton),
                        start = Offset(x - size.width / 2, 0f),
                        end = Offset(x + size.width / 2, 0f),
                    ),
                )
            },
    )
}

/** Skeleton shaped like [CryptoListItem], so loading does not shift the layout when data arrives. */
@Composable
fun CoinRowSkeleton(modifier: Modifier = Modifier) {
    val spacing = CryptoManiaTheme.spacing
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(CryptoManiaTheme.sizes.listItemMinHeight)
            .padding(horizontal = spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBlock(Modifier.clip(CircleShape), width = CryptoManiaTheme.sizes.logoMedium, height = CryptoManiaTheme.sizes.logoMedium)
        Spacer(Modifier.width(spacing.md))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBlock(width = 96.dp)
            SkeletonBlock(width = 48.dp, height = 10.dp)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SkeletonBlock(width = 72.dp)
            SkeletonBlock(width = 44.dp, height = 10.dp)
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = CryptoManiaTheme.colors
    Column(
        modifier = modifier
            .widthIn(max = 420.dp)
            .padding(horizontal = CryptoManiaTheme.spacing.xxl, vertical = CryptoManiaTheme.spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CryptoManiaTheme.spacing.sm),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.surfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = colors.textSecondary)
        }
        Spacer(Modifier.height(CryptoManiaTheme.spacing.xs))
        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(CryptoManiaTheme.spacing.xs))
            action()
        }
    }
}

@Composable
fun ErrorState(
    error: AppError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        icon = if (error == AppError.NetworkUnavailable) Icons.Rounded.WifiOff else Icons.Rounded.ErrorOutline,
        title = stringResource(error.titleRes()),
        message = stringResource(error.messageRes()),
        modifier = modifier.semantics { liveRegion = LiveRegionMode.Polite },
        action = {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.action_retry)) }
        },
    )
}

/**
 * Slim banner describing data freshness. Hidden when everything is up to date; never a
 * dialog, never blocks content.
 */
@Composable
fun NetworkStatusIndicator(status: SyncStatus, modifier: Modifier = Modifier) {
    // Keep rendering the last visible status while the banner animates out.
    var shown by remember { mutableStateOf(status) }
    if (status !is SyncStatus.UpToDate) shown = status
    AnimatedVisibility(
        visible = status !is SyncStatus.UpToDate,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier,
    ) {
        val colors = CryptoManiaTheme.colors
        val (icon, text) = when (val current = shown) {
            is SyncStatus.Offline -> Icons.Rounded.CloudOff to if (current.dataFrom != null) {
                stringResource(R.string.status_offline, rememberRelativeTime(current.dataFrom))
            } else {
                stringResource(R.string.status_offline_no_time)
            }
            is SyncStatus.Degraded -> {
                val reason = stringResource(current.error.shortRes())
                val icon = if (current.error == AppError.RateLimited) Icons.Rounded.HourglassTop else Icons.Rounded.ErrorOutline
                icon to if (current.dataFrom != null) {
                    stringResource(R.string.status_stale, reason, rememberRelativeTime(current.dataFrom))
                } else {
                    reason
                }
            }
            SyncStatus.UpToDate -> Icons.Rounded.CloudOff to ""
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.warningContainer)
                .padding(horizontal = CryptoManiaTheme.spacing.lg, vertical = CryptoManiaTheme.spacing.sm)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CryptoManiaTheme.spacing.sm),
        ) {
            Icon(icon, contentDescription = null, tint = colors.warning, modifier = Modifier.size(16.dp))
            Text(text, style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoManiaPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    val colors = CryptoManiaTheme.colors
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = colors.surfaceRaised,
                color = colors.brand,
            )
        },
        content = content,
    )
}
