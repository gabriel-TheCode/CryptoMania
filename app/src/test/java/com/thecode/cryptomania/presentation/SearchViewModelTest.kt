package com.thecode.cryptomania.presentation

import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.CoinSearchHit
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.usecase.RankLocalSearchUseCase
import com.thecode.cryptomania.presentation.feature.search.RemoteResults
import com.thecode.cryptomania.presentation.feature.search.SearchEffect
import com.thecode.cryptomania.presentation.feature.search.SearchIntent
import com.thecode.cryptomania.presentation.feature.search.SearchViewModel
import com.thecode.cryptomania.testutil.FakeMarketRepository
import com.thecode.cryptomania.testutil.FakeSettingsRepository
import com.thecode.cryptomania.testutil.MainDispatcherRule
import com.thecode.cryptomania.testutil.coin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule val mainDispatcher = MainDispatcherRule(StandardTestDispatcher())

    private val market = FakeMarketRepository().apply {
        topCoins.value = Cached(
            (1..8).map { coin("bit-$it", rank = it, symbol = "BT$it", name = "Bit $it") } + coin("ethereum", rank = 9, symbol = "ETH", name = "Ethereum"),
            Instant.EPOCH,
        )
        searchResult = Outcome.Success(listOf(CoinSearchHit("pepecoin", "PepeCoin", "PEPECOIN", null, 1045)))
    }
    private val settings = FakeSettingsRepository()

    private fun viewModel() = SearchViewModel(market, settings, RankLocalSearchUseCase(), mainDispatcher.dispatcher)

    @Test
    fun `typing is debounced into a single remote call`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        listOf("p", "pe", "pep", "pepe").forEach {
            vm.onIntent(SearchIntent.QueryChanged(it))
            advanceTimeBy(100)
        }
        advanceUntilIdle()

        assertEquals(listOf("pepe"), market.searchQueries)
        val remote = vm.state.value.remote as RemoteResults.Loaded
        assertEquals("pepecoin", remote.hits.single().id)
    }

    @Test
    fun `enough local matches means no network call`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onIntent(SearchIntent.QueryChanged("bit"))
        advanceUntilIdle()

        assertEquals(8, vm.state.value.localResults.size)
        assertTrue(market.searchQueries.isEmpty())
        assertEquals(RemoteResults.Idle, vm.state.value.remote)
    }

    @Test
    fun `submitting forces a remote lookup and remembers the query`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onIntent(SearchIntent.QueryChanged("bit"))
        advanceUntilIdle()
        vm.onIntent(SearchIntent.Submit)
        advanceUntilIdle()

        assertEquals(listOf("bit"), market.searchQueries)
        assertEquals(listOf("bit"), settings.recentSearches.value)
    }

    @Test
    fun `no match anywhere shows the empty state`() = runTest(mainDispatcher.dispatcher) {
        market.searchResult = Outcome.Success(emptyList())
        val vm = viewModel()
        backgroundScope.launch { vm.state.collect {} }

        vm.onIntent(SearchIntent.QueryChanged("zzzz"))
        advanceUntilIdle()

        assertTrue(vm.state.value.showNoResults)
    }

    @Test
    fun `selecting a result opens the coin`() = runTest(mainDispatcher.dispatcher) {
        val vm = viewModel()
        vm.onIntent(SearchIntent.ResultSelected("ethereum"))
        assertEquals(SearchEffect.OpenCoin("ethereum"), vm.effect.first())
    }
}
