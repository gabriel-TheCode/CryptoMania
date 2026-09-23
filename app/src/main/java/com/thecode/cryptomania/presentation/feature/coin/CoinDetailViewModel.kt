package com.thecode.cryptomania.presentation.feature.coin

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.R
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.domain.model.CoinProfile
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.model.PriceHistory
import com.thecode.cryptomania.domain.model.PricePoint
import com.thecode.cryptomania.domain.repository.CoinDetailsRepository
import com.thecode.cryptomania.domain.repository.MarketRepository
import com.thecode.cryptomania.domain.repository.NetworkMonitor
import com.thecode.cryptomania.domain.repository.WatchlistRepository
import com.thecode.cryptomania.presentation.model.ChangeUi
import com.thecode.cryptomania.presentation.model.Trend
import com.thecode.cryptomania.presentation.util.Formatters
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.presentation.util.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface CoinDetailIntent {
    data class RangeSelected(val range: ChartRange) : CoinDetailIntent
    data object ToggleWatchlist : CoinDetailIntent
    data object Retry : CoinDetailIntent
    data object ScreenResumed : CoinDetailIntent
}

@Immutable
data class CoinDetailUiState(
    val content: ScreenContent<CoinDetailContent> = ScreenContent.Loading,
    val range: ChartRange = ChartRange.OneDay,
    val chart: ChartUiState = ChartUiState.Loading,
    val isWatched: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.UpToDate,
)

@Immutable
sealed interface ChartUiState {
    data object Loading : ChartUiState
    data object Empty : ChartUiState
    data class Failed(val error: AppError) : ChartUiState
    data class Ready(val chart: ChartUi) : ChartUiState
}

@Immutable
data class ChartUi(
    val range: ChartRange,
    val points: List<PricePoint>,
    val trend: Trend,
    val change: ChangeUi,
    val high: String,
    val low: String,
    val startPrice: String,
    val endPrice: String,
)

@Immutable
data class StatUi(@param:StringRes val label: Int, val value: String, val detail: String? = null, val progress: Float? = null)

@Immutable
data class CoinDetailContent(
    val id: String,
    val name: String,
    val symbol: String,
    val logoUrl: String?,
    val rank: String,
    val price: String,
    val priceValue: Double?,
    val change24h: ChangeUi,
    val performance: List<Pair<Int, ChangeUi>>,
    val stats: List<StatUi>,
    val low24h: String,
    val high24h: String,
    /** Position of the current price inside today's range, 0..1, or null when unknown. */
    val dayRangePosition: Float?,
    val profile: CoinProfile?,
    val fetchedAt: Instant?,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val marketRepository: MarketRepository,
    private val detailsRepository: CoinDetailsRepository,
    private val watchlistRepository: WatchlistRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    val coinId: String = checkNotNull(savedStateHandle[COIN_ID_ARG]) { "Missing coinId argument" }

    private val formatters = Formatters()
    private val range = MutableStateFlow(ChartRange.OneDay)
    private val coinError = MutableStateFlow<AppError?>(null)
    private val chartError = MutableStateFlow<Pair<ChartRange, AppError>?>(null)
    private val coinLoading = MutableStateFlow(true)
    private var chartJob: Job? = null

    private val coinContent = combine(
        marketRepository.observeCoin(coinId),
        detailsRepository.observeProfile(coinId),
    ) { coin, profile -> coin?.toContent(profile) }
        .flowOn(Dispatchers.Default)

    private val chart = range.flatMapLatest { selected ->
        detailsRepository.observePriceHistory(coinId, selected).map { selected to it }
    }.combine(chartError) { (selected, cached), error ->
        chartState(selected, cached, error?.takeIf { it.first == selected }?.second)
    }.flowOn(Dispatchers.Default)

    private val syncInputs = combine(networkMonitor.isOnline, coinError, chartError) { online, coinErr, chartErr ->
        Triple(online, coinErr, chartErr?.second)
    }

    val state: StateFlow<CoinDetailUiState> = combine(
        coinContent,
        chart,
        range,
        watchlistRepository.observeWatchlist().map { coinId in it },
        combine(syncInputs, coinLoading) { sync, loading -> sync to loading },
    ) { content, chart, range, watched, (sync, loading) ->
        val (online, coinErr, chartErr) = sync
        CoinDetailUiState(
            content = when {
                content != null -> ScreenContent.Ready(content)
                coinErr != null && !loading -> ScreenContent.Failed(coinErr)
                else -> ScreenContent.Loading
            },
            range = range,
            chart = chart,
            isWatched = watched,
            syncStatus = if (content == null) SyncStatus.UpToDate else SyncStatus.from(online, coinErr ?: chartErr, content.fetchedAt),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CoinDetailUiState())

    init {
        refreshCoin()
        refreshChart(ChartRange.OneDay)
        // Slow-changing metadata: at most one request per coin per day, failures are silent.
        viewModelScope.launch { detailsRepository.refreshProfile(coinId) }
        networkMonitor.isOnline.distinctUntilChanged().drop(1).filter { it }
            .onEach { refreshCoin(); refreshChart(range.value) }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: CoinDetailIntent) {
        when (intent) {
            is CoinDetailIntent.RangeSelected -> {
                range.value = intent.range
                refreshChart(intent.range)
            }
            CoinDetailIntent.ToggleWatchlist -> viewModelScope.launch { watchlistRepository.toggle(coinId) }
            CoinDetailIntent.Retry -> {
                refreshCoin(force = true)
                refreshChart(range.value, force = true)
            }
            CoinDetailIntent.ScreenResumed -> {
                refreshCoin()
                refreshChart(range.value)
            }
        }
    }

    private fun refreshCoin(force: Boolean = false) {
        viewModelScope.launch {
            coinLoading.value = true
            val outcome = marketRepository.refreshCoin(coinId, force)
            coinError.value = (outcome as? Outcome.Failure)?.error
            coinLoading.value = false
        }
    }

    /** Range switches cancel the previous chart request; cached ranges cost no network call. */
    private fun refreshChart(selected: ChartRange, force: Boolean = false) {
        chartJob?.cancel()
        chartJob = viewModelScope.launch {
            val outcome = detailsRepository.refreshPriceHistory(coinId, selected, force)
            chartError.value = (outcome as? Outcome.Failure)?.let { selected to it.error }
        }
    }

    private fun chartState(selected: ChartRange, cached: Cached<PriceHistory>?, error: AppError?): ChartUiState {
        val history = cached?.value
        return when {
            history == null && error != null -> ChartUiState.Failed(error)
            history == null -> ChartUiState.Loading
            history.points.size < 2 -> ChartUiState.Empty
            else -> {
                val change = history.changePercent
                ChartUiState.Ready(
                    ChartUi(
                        range = selected,
                        points = history.points,
                        trend = when {
                            change == null || change == 0.0 -> Trend.Flat
                            change > 0 -> Trend.Up
                            else -> Trend.Down
                        },
                        change = ChangeUi.of(change, formatters),
                        high = formatters.price(history.high),
                        low = formatters.price(history.low),
                        startPrice = formatters.price(history.first?.price),
                        endPrice = formatters.price(history.last?.price),
                    ),
                )
            }
        }
    }

    private fun Coin.toContent(profile: CoinProfile?): CoinDetailContent {
        val supplyProgress = circulatingSupply?.let { circulating ->
            maxSupply?.takeIf { it > 0 }?.let { (circulating / it).toFloat().coerceIn(0f, 1f) }
        }
        val stats = buildList {
            add(StatUi(R.string.stat_market_cap, formatters.compactCurrency(marketCap)))
            add(StatUi(R.string.stat_volume, formatters.compactCurrency(volume24h)))
            add(StatUi(R.string.stat_rank, formatters.rank(marketCapRank)))
            if (fullyDilutedValuation != null) add(StatUi(R.string.stat_fdv, formatters.compactCurrency(fullyDilutedValuation)))
            add(
                StatUi(
                    label = R.string.stat_circulating,
                    value = formatters.compactNumber(circulatingSupply),
                    detail = supplyProgress?.let { formatters.percent(it * 100.0, signed = false) },
                    progress = supplyProgress,
                ),
            )
            add(StatUi(R.string.stat_max_supply, formatters.compactNumber(maxSupply)))
            if (allTimeHigh != null) {
                add(
                    StatUi(
                        label = R.string.stat_ath,
                        value = formatters.price(allTimeHigh),
                        detail = listOfNotNull(
                            allTimeHighChangePercent?.let(formatters::percent),
                            allTimeHighDate?.let(formatters::date),
                        ).joinToString(" · ").ifEmpty { null },
                    ),
                )
            }
        }
        val dayPosition = if (price != null && low24h != null && high24h != null && high24h > low24h) {
            ((price - low24h) / (high24h - low24h)).toFloat().coerceIn(0f, 1f)
        } else {
            null
        }
        return CoinDetailContent(
            id = id,
            name = name,
            symbol = symbol,
            logoUrl = imageUrl,
            rank = formatters.rank(marketCapRank),
            price = formatters.price(price),
            priceValue = price,
            change24h = ChangeUi.of(change24hPercent, formatters),
            performance = listOf(
                R.string.stat_change_1h to ChangeUi.of(change1hPercent, formatters),
                R.string.stat_change_24h to ChangeUi.of(change24hPercent, formatters),
                R.string.stat_change_7d to ChangeUi.of(change7dPercent, formatters),
            ),
            stats = stats,
            low24h = formatters.price(low24h),
            high24h = formatters.price(high24h),
            dayRangePosition = dayPosition,
            profile = profile,
            fetchedAt = lastUpdated,
        )
    }

    companion object {
        const val COIN_ID_ARG = "coinId"
    }
}
