package com.thecode.cryptomania.presentation.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.presentation.designsystem.component.chart.PriceChart
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.model.ChangeUi
import com.thecode.cryptomania.presentation.model.CoinRowUi
import com.thecode.cryptomania.presentation.model.Trend
import kotlin.math.sin

private val sampleRow = CoinRowUi(
    id = "bitcoin", name = "Bitcoin", symbol = "BTC", logoUrl = null, rank = "1", price = "$84,456.00",
    priceValue = 84_456.0, change24h = ChangeUi(Trend.Down, "-1.93%"), marketCap = "$1.70T", volume = "$44.43B",
    sparkline = List(48) { 75_000 + 1_500 * sin(it / 6.0) + it * 150 }, sparklineTrend = Trend.Up, isWatched = false,
)

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ComponentsPreview() {
    CryptoManiaTheme {
        Column(
            Modifier
                .background(CryptoManiaTheme.colors.background)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CryptoListItem(coin = sampleRow, onClick = {})
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PriceChangeBadge(ChangeUi(Trend.Up, "+4.12%"))
                PriceChangeBadge(ChangeUi(Trend.Down, "-2.30%"))
                PriceChangeBadge(ChangeUi(Trend.Flat, "0.00%"))
            }
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CryptoManiaFilterChip("All", selected = true, onClick = {})
                CryptoManiaFilterChip("Gainers", selected = false, onClick = {})
            }
            CoinRowSkeleton()
        }
    }
}

@Preview(name = "Chart", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PriceChartPreview() {
    CryptoManiaTheme {
        PriceChart(
            points = List(120) { PricePoint(it * 60_000L, 100 + 8 * sin(it / 9.0) + it * 0.1) },
            lineColor = CryptoManiaTheme.colors.positive,
            highLabel = "High $120.00",
            lowLabel = "Low $92.00",
            accessibilityDescription = "Preview chart",
            onScrub = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(CryptoManiaTheme.colors.background),
        )
    }
}
