package com.thecode.cryptomania.presentation

import androidx.lifecycle.SavedStateHandle
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.model.PriceHistory
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.presentation.feature.coin.ChartUiState
import com.thecode.cryptomania.presentation.feature.coin.CoinDetailIntent
import com.thecode.cryptomania.presentation.feature.coin.CoinDetailViewModel
import com.thecode.cryptomania.presentation.model.Trend
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.testutil.FakeCoinDetailsRepository
import com.thecode.cryptomania.testutil.FakeMarketRepository
import com.thecode.cryptomania.testutil.FakeNetworkMonitor
import com.thecode.cryptomania.testutil.FakeWatchlistRepository
import com.thecode.cryptomania.testutil.MainDispatcherRule
import com.thecode.cryptomania.testutil.coin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class CoinDetailViewModelTest {

    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val market = FakeMarketRepository().apply { topCoins.value = Cached(listOf(coin("bitcoin")), Instant.EPOCH) }
    private val details = FakeCoinDetailsRepository()
    private val watchlist = FakeWatchlistRepository()

    private fun viewModel() = CoinDetailViewModel(
        SavedStateHandle(mapOf(CoinDetailViewModel.COIN_ID_ARG to "bitcoin")),
        market,
        details,
        watchlist,
        FakeNetworkMonitor(),
        mainDispatcher.dispatcher,
    )

    private fun history(range: ChartRange, vararg prices: Double) =
        Cached(PriceHistory("bitcoin", range, prices.mapIndexed { i, p -> PricePoint(i.toLong(), p) }), Instant.EPOCH)

    @Test
    fun `changing range loads that range and reports its change`() = runTest(mainDispatcher.dispatcher) {
        details.history(ChartRange.OneWeek).value = history(ChartRange.OneWeek, 100.0, 90.0)
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onIntent(CoinDetailIntent.RangeSelected(ChartRange.OneWeek))

        val chart = (vm.state.value.chart as ChartUiState.Ready).chart
        assertEquals(ChartRange.OneWeek, chart.range)
        assertEquals(Trend.Down, chart.trend)
        assertEquals(ChartRange.OneWeek, details.refreshedRanges.last())
    }

    @Test
    fun `chart failure without cache stays local to the chart`() = runTest(mainDispatcher.dispatcher) {
        details.chartResult = Outcome.Failure(AppError.NetworkUnavailable)
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        assertTrue(vm.state.value.content is ScreenContent.Ready)
        assertEquals(ChartUiState.Failed(AppError.NetworkUnavailable), vm.state.value.chart)
    }

    @Test
    fun `sparse history is reported as empty rather than drawn`() = runTest(mainDispatcher.dispatcher) {
        details.history(ChartRange.OneDay).value = history(ChartRange.OneDay, 100.0)
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        assertEquals(ChartUiState.Empty, vm.state.value.chart)
    }

    @Test
    fun `watchlist toggles`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onIntent(CoinDetailIntent.ToggleWatchlist)
        assertTrue(vm.state.value.isWatched)
        vm.onIntent(CoinDetailIntent.ToggleWatchlist)
        assertEquals(false, vm.state.value.isWatched)
    }

    @Test
    fun `unknown coin that cannot be fetched shows an error`() = runTest(mainDispatcher.dispatcher) {
        market.topCoins.value = null
        market.refreshCoinResult = Outcome.Failure(AppError.NotFound)
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        assertEquals(ScreenContent.Failed(AppError.NotFound), vm.state.value.content)
    }
}
