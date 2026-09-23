package com.thecode.cryptomania.presentation.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.designsystem.theme.Motion
import com.thecode.cryptomania.presentation.model.ChangeUi
import com.thecode.cryptomania.presentation.model.Trend
import kotlinx.coroutines.delay

@Composable
fun Trend.contentColor(): Color = when (this) {
    Trend.Up -> CryptoManiaTheme.colors.positive
    Trend.Down -> CryptoManiaTheme.colors.negative
    Trend.Flat, Trend.Unknown -> CryptoManiaTheme.colors.textSecondary
}

@Composable
fun Trend.containerColor(): Color = when (this) {
    Trend.Up -> CryptoManiaTheme.colors.positiveContainer
    Trend.Down -> CryptoManiaTheme.colors.negativeContainer
    Trend.Flat, Trend.Unknown -> CryptoManiaTheme.colors.surfaceHigh
}

@Composable
fun ChangeUi.accessibilityLabel(): String = when (trend) {
    Trend.Up -> stringResource(R.string.cd_change_up, text.removePrefix("+"))
    Trend.Down -> stringResource(R.string.cd_change_down, text.removePrefix("-"))
    Trend.Flat -> stringResource(R.string.cd_change_flat)
    Trend.Unknown -> text
}

/**
 * Price that briefly tints toward the direction of a new tick, then settles back.
 * Communicates "this just changed" without any layout movement.
 */
@Composable
fun CryptoPrice(
    text: String,
    value: Double?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = CryptoManiaTheme.colors.textPrimary,
) {
    var previous by remember { mutableStateOf(value) }
    var flash by remember { mutableStateOf(Trend.Flat) }
    LaunchedEffect(value) {
        val old = previous
        previous = value
        if (old != null && value != null && old != value) {
            flash = if (value > old) Trend.Up else Trend.Down
            delay(FLASH_MILLIS)
            flash = Trend.Flat
        }
    }
    val tint by animateColorAsState(
        targetValue = if (flash == Trend.Flat) color else flash.contentColor(),
        animationSpec = Motion.effects(),
        label = "priceTint",
    )
    Text(text = text, style = style, color = tint, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

enum class BadgeStyle { Filled, Plain }

@Composable
fun PriceChangeBadge(
    change: ChangeUi,
    modifier: Modifier = Modifier,
    style: BadgeStyle = BadgeStyle.Filled,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
) {
    val content = change.trend.contentColor()
    val label = change.accessibilityLabel()
    Row(
        modifier = modifier
            .then(
                if (style == BadgeStyle.Filled) {
                    Modifier
                        .background(change.trend.containerColor(), MaterialTheme.shapes.extraSmall)
                        .padding(start = 2.dp, end = 6.dp, top = 2.dp, bottom = 2.dp)
                } else {
                    Modifier
                },
            )
            .clearAndSetSemantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val icon = when (change.trend) {
            Trend.Up -> Icons.Rounded.ArrowDropUp
            Trend.Down -> Icons.Rounded.ArrowDropDown
            Trend.Flat -> Icons.AutoMirrored.Rounded.TrendingFlat
            Trend.Unknown -> null
        }
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        Text(text = change.text.removePrefix("+").removePrefix("-"), style = textStyle, color = content, maxLines = 1)
    }
}

@Composable
fun MarketStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supporting: @Composable (() -> Unit)? = null,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = CryptoManiaTheme.colors.textSecondary, maxLines = 1)
        Text(value, style = MaterialTheme.typography.titleMedium, color = CryptoManiaTheme.colors.textPrimary, maxLines = 1)
        supporting?.invoke()
    }
}

private const val FLASH_MILLIS = 900L
