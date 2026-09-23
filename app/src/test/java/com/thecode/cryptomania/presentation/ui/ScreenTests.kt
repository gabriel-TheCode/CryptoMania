package com.thecode.cryptomania.presentation.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.domain.usecase.MarketFilter
import com.thecode.cryptomania.presentation.designsystem.theme.CryptoManiaTheme
import com.thecode.cryptomania.presentation.feature.coin.ChartUi
import com.thecode.cryptomania.presentation.feature.coin.ChartUiState
import com.thecode.cryptomania.presentation.feature.coin.CoinDetailContent
import com.thecode.cryptomania.presentation.feature.coin.CoinDetailIntent
import com.thecode.cryptomania.presentation.feature.coin.CoinDetailScreen
import com.thecode.cryptomania.presentation.feature.coin.CoinDetailUiState
import com.thecode.cryptomania.presentation.feature.market.MarketContent
import com.thecode.cryptomania.presentation.feature.market.MarketIntent
import com.thecode.cryptomania.presentation.feature.market.MarketScreen
import com.thecode.cryptomania.presentation.feature.market.MarketUiState
import com.thecode.cryptomania.presentation.feature.search.RemoteResults
import com.thecode.cryptomania.presentation.feature.search.SearchIntent
import com.thecode.cryptomania.presentation.feature.search.SearchScreen
import com.thecode.cryptomania.presentation.feature.search.SearchUiState
import com.thecode.cryptomania.presentation.model.toRowUi
import com.thecode.cryptomania.presentation.model.ChangeUi
import com.thecode.cryptomania.presentation.model.Trend
import com.thecode.cryptomania.presentation.util.Formatters
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.presentation.util.SyncStatus
import com.thecode.cryptomania.testutil.coin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.util.Locale

private val formatters = Formatters(Locale.US)
private val rows = listOf(coin("bitcoin", name = "Bitcoin", symbol = "BTC"), coin("ethereum", rank = 2, name = "Ethereum", symbol = "ETH"))
    .map { it.toRowUi(formatters, isWatched = false) }

@RunWith(RobolectricTestRunner::class)
class MarketScreenTest {
    @get:Rule val compose = createComposeRule()
    private val intents = mutableListOf<MarketIntent>()
    private val opened = mutableListOf<String>()

    private fun show(state: MarketUiState) = compose.setContent {
        CryptoManiaTheme { MarketScreen(state, { intents += it }, { opened += it }, {}) }
    }

    private val ready = MarketUiState(content = ScreenContent.Ready(MarketContent(null, emptyList(), rows, Instant.now())))

    @Test
    fun marketLoadsAndOpensCoin() {
        show(ready)
        compose.onNode(hasContentDescription("Bitcoin", substring = true)).assertIsDisplayed().performClick()
        assertEquals(listOf("bitcoin"), opened)
    }

    @Test
    fun pullToRefreshSendsRefresh() {
        show(ready)
        compose.onNodeWithTag("market_list").performTouchInput { swipeDown() }
        compose.waitForIdle()
        assertTrue(MarketIntent.Refresh in intents)
    }

    @Test
    fun filterChipSelectsFilter() {
        show(ready)
        compose.onNodeWithText("Watchlist").performClick()
        assertEquals(MarketIntent.FilterSelected(MarketFilter.Watchlist), intents.last())
    }

    @Test
    fun errorWithoutCacheOffersRetry() {
        show(MarketUiState(content = ScreenContent.Failed(AppError.RateLimited)))
        compose.onNodeWithText("Too many requests").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(listOf<MarketIntent>(MarketIntent.Refresh), intents)
    }

    @Test
    fun offlineKeepsDataAndShowsBanner() {
        show(ready.copy(syncStatus = SyncStatus.Offline(Instant.now())))
        compose.onNodeWithText("Offline", substring = true).assertIsDisplayed()
        compose.onNode(hasContentDescription("Ethereum", substring = true)).assertIsDisplayed()
    }
}

@RunWith(RobolectricTestRunner::class)
class CoinDetailScreenTest {
    @get:Rule val compose = createComposeRule()
    private val intents = mutableListOf<CoinDetailIntent>()

    private val content = CoinDetailContent(
        id = "bitcoin", name = "Bitcoin", symbol = "BTC", logoUrl = null, rank = "#1", price = "$84,456.00",
        priceValue = 84_456.0, change24h = ChangeUi(Trend.Down, "-1.93%"), performance = emptyList(), stats = emptyList(),
        low24h = "$83,991.00", high24h = "$87,251.00", dayRangePosition = 0.3f, profile = null, fetchedAt = null,
    )
    private val chart = ChartUi(
        range = ChartRange.OneDay, points = listOf(PricePoint(0, 1.0), PricePoint(1, 2.0)), trend = Trend.Up,
        change = ChangeUi(Trend.Up, "+100.00%"), high = "$2.00", low = "$1.00", startPrice = "$1.00", endPrice = "$2.00",
    )

    private fun show(state: CoinDetailUiState) = compose.setContent {
        CryptoManiaTheme { CoinDetailScreen(state, { intents += it }, {}) }
    }

    @Test
    fun changingChartPeriodSendsRange() {
        show(CoinDetailUiState(content = ScreenContent.Ready(content), chart = ChartUiState.Ready(chart)))
        compose.onNode(hasContentDescription("week range")).performScrollTo().performClick()
        assertEquals(CoinDetailIntent.RangeSelected(ChartRange.OneWeek), intents.last())
    }

    @Test
    fun chartIsDescribedForAccessibility() {
        show(CoinDetailUiState(content = ScreenContent.Ready(content), chart = ChartUiState.Ready(chart)))
        compose.onNode(hasContentDescription("Price chart for the past 24 hours", substring = true)).assertIsDisplayed()
    }

    @Test
    fun chartErrorIsLocalAndRetryable() {
        show(CoinDetailUiState(content = ScreenContent.Ready(content), chart = ChartUiState.Failed(AppError.NetworkUnavailable)))
        compose.onNodeWithText("Chart unavailable").assertIsDisplayed()
        compose.onNodeWithText("$84,456.00").assertIsDisplayed()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(CoinDetailIntent.Retry, intents.last())
    }

    @Test
    fun starTogglesWatchlist() {
        show(CoinDetailUiState(content = ScreenContent.Ready(content), chart = ChartUiState.Ready(chart)))
        compose.onNode(hasContentDescription("Add to watchlist")).performClick()
        assertEquals(CoinDetailIntent.ToggleWatchlist, intents.last())
    }
}

@RunWith(RobolectricTestRunner::class)
class SearchScreenTest {
    @get:Rule val compose = createComposeRule()
    private val intents = mutableListOf<SearchIntent>()

    private fun show(state: SearchUiState) = compose.setContent {
        CryptoManiaTheme { SearchScreen(state, { intents += it }, {}) }
    }

    @Test
    fun typingReachesTheViewModel() {
        show(SearchUiState())
        compose.onNodeWithTag("search_field").performTextInput("pepe")
        compose.waitForIdle()
        assertEquals(SearchIntent.QueryChanged("pepe"), intents.last())
    }

    @Test
    fun recentSearchesAreOffered() {
        show(SearchUiState(recentSearches = listOf("solana")))
        compose.onNodeWithText("Recent searches").assertIsDisplayed()
        compose.onNodeWithText("solana").performClick()
        compose.waitForIdle()
        assertEquals(SearchIntent.QueryChanged("solana"), intents.last())
    }

    @Test
    fun emptyResultsExplainThemselves() {
        show(SearchUiState(query = "zzzz", remote = RemoteResults.Loaded(emptyList())))
        compose.onNodeWithText("No results for “zzzz”").assertIsDisplayed()
    }
}
