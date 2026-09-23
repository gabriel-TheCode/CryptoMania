package com.thecode.cryptomania.presentation.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thecode.cryptomania.R
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.model.CoinRowUi

/**
 * Market row. On wide layouts ([expanded]) market cap and volume get their own columns
 * instead of being hidden, so tablets show more data rather than more whitespace.
 */
@Composable
fun CryptoListItem(
    coin: CoinRowUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    showRank: Boolean = true,
) {
    val colors = CryptoManiaTheme.colors
    val spacing = CryptoManiaTheme.spacing
    val description = stringResource(R.string.cd_coin_row, coin.name, coin.symbol, coin.price, coin.change24h.accessibilityLabel())
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = CryptoManiaTheme.sizes.listItemMinHeight)
            .clickable(onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
            }
            .padding(horizontal = spacing.lg, vertical = spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showRank) {
            Text(
                text = coin.rank,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary,
                textAlign = TextAlign.Start,
                maxLines = 1,
                modifier = Modifier.width(30.dp),
            )
        }
        CoinLogo(url = coin.logoUrl, fallbackLabel = coin.symbol)
        Spacer(Modifier.width(spacing.md))
        Column(Modifier.weight(1f)) {
            Text(
                text = coin.name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(coin.symbol, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary, maxLines = 1)
        }
        if (expanded) {
            NumericCell(coin.marketCap, Modifier.width(112.dp))
            NumericCell(coin.volume, Modifier.width(112.dp))
        }
        Sparkline(
            values = coin.sparkline,
            color = coin.sparklineTrend.contentColor(),
            modifier = Modifier
                .padding(horizontal = spacing.md)
                .size(CryptoManiaTheme.sizes.sparklineWidth, CryptoManiaTheme.sizes.sparklineHeight),
        )
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 92.dp)) {
            CryptoPrice(text = coin.price, value = coin.priceValue, style = MaterialTheme.typography.titleSmall)
            PriceChangeBadge(coin.change24h, style = BadgeStyle.Plain, textStyle = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun NumericCell(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = CryptoManiaTheme.colors.textSecondary,
        textAlign = TextAlign.End,
        maxLines = 1,
        modifier = modifier,
    )
}

/** Compact card for the "top movers" carousel. */
@Composable
fun MoverCard(
    coin: CoinRowUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The clickable card merges its children: TalkBack reads symbol, price and change.
    CryptoManiaCard(onClick = onClick, modifier = modifier.width(152.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CoinLogo(url = coin.logoUrl, fallbackLabel = coin.symbol, size = CryptoManiaTheme.sizes.logoSmall)
            Text(
                coin.symbol,
                style = MaterialTheme.typography.titleSmall,
                color = CryptoManiaTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.size(CryptoManiaTheme.spacing.md))
        Text(coin.price, style = MaterialTheme.typography.titleMedium, color = CryptoManiaTheme.colors.textPrimary, maxLines = 1)
        Spacer(Modifier.size(CryptoManiaTheme.spacing.xs))
        PriceChangeBadge(coin.change24h)
    }
}
