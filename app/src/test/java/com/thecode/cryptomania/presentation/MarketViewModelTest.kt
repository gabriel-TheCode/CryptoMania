package com.thecode.cryptomania.presentation

import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.Done
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.usecase.MarketFilter
import com.thecode.cryptomania.domain.usecase.ObserveMarketOverviewUseCase
import com.thecode.cryptomania.presentation.feature.market.MarketIntent
import com.thecode.cryptomania.presentation.feature.market.MarketViewModel
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.presentation.util.SyncStatus
import com.thecode.cryptomania.testutil.FakeMarketRepository
import com.thecode.cryptomania.testutil.FakeNetworkMonitor
import com.thecode.cryptomania.testutil.FakeWatchlistRepository
import com.thecode.cryptomania.testutil.MainDispatcherRule
import com.thecode.cryptomania.testutil.coin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class MarketViewModelTest {

    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val market = FakeMarketRepository()
    private val watchlist = FakeWatchlistRepository()
    private val network = FakeNetworkMonitor()

    private fun viewModel() = MarketViewModel(
        ObserveMarketOverviewUseCase(market, watchlist),
        market,
        watchlist,
        network,
        mainDispatcher.dispatcher,
    )

    private val cached = Cached(listOf(coin("bitcoin", change24h = -1.0), coin("ethereum", rank = 2, change24h = 3.0)), Instant.EPOCH)

    @Test
    fun `cached market is shown as ready`() = runTest(mainDispatcher.dispatcher) {
        market.topCoins.value = cached
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        val content = vm.state.value.content as ScreenContent.Ready
        assertEquals(listOf("bitcoin", "ethereum"), content.data.coins.map { it.id })
        assertEquals(SyncStatus.UpToDate, vm.state.value.syncStatus)
    }

    @Test
    fun `failure without cache shows an error, with cache a banner`() = runTest(mainDispatcher.dispatcher) {
        market.refreshResult = Outcome.Failure(AppError.RateLimited)
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        assertEquals(ScreenContent.Failed(AppError.RateLimited), vm.state.value.content)

        market.topCoins.value = cached
        assertTrue(vm.state.value.content is ScreenContent.Ready)
        assertEquals(SyncStatus.Degraded(AppError.RateLimited, Instant.EPOCH), vm.state.value.syncStatus)
    }

    @Test
    fun `offline is reported without hiding cached data`() = runTest(mainDispatcher.dispatcher) {
        market.topCoins.value = cached
        network.isOnline.value = false
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        assertTrue(vm.state.value.content is ScreenContent.Ready)
        assertEquals(SyncStatus.Offline(Instant.EPOCH), vm.state.value.syncStatus)
    }

    @Test
    fun `filters are applied locally`() = runTest(mainDispatcher.dispatcher) {
        market.topCoins.value = cached
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }
        val callsBefore = market.refreshCalls.size

        vm.onIntent(MarketIntent.FilterSelected(MarketFilter.Losers))

        val coins = (vm.state.first { it.filter == MarketFilter.Losers }.content as ScreenContent.Ready).data.coins
        assertEquals(listOf("bitcoin"), coins.map { it.id })
        assertEquals(callsBefore, market.refreshCalls.size)
    }

    @Test
    fun `pull to refresh forces, resume does not, reconnect refreshes`() = runTest(mainDispatcher.dispatcher) {
        market.refreshResult = Done
        network.isOnline.value = false
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onIntent(MarketIntent.Refresh)
        vm.onIntent(MarketIntent.ScreenResumed)
        network.isOnline.value = true

        assertEquals(listOf(false, true, false, false), market.refreshCalls)
    }
}
