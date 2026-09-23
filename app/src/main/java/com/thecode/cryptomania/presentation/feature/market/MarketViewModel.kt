package com.thecode.cryptomania.presentation.feature.market

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.repository.MarketRepository
import com.thecode.cryptomania.domain.repository.NetworkMonitor
import com.thecode.cryptomania.domain.usecase.MarketFilter
import com.thecode.cryptomania.domain.usecase.MarketOverview
import com.thecode.cryptomania.domain.usecase.ObserveMarketOverviewUseCase
import com.thecode.cryptomania.presentation.model.ChangeUi
import com.thecode.cryptomania.presentation.model.CoinRowUi
import com.thecode.cryptomania.presentation.model.toRowUi
import com.thecode.cryptomania.presentation.util.Formatters
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.presentation.util.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface MarketIntent {
    data object Refresh : MarketIntent
    data object ScreenResumed : MarketIntent
    data class FilterSelected(val filter: MarketFilter) : MarketIntent
}

@Immutable
data class MarketUiState(
    val content: ScreenContent<MarketContent> = ScreenContent.Loading,
    val filter: MarketFilter = MarketFilter.All,
    val isRefreshing: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.UpToDate,
)

@Immutable
data class MarketContent(
    val global: GlobalMarketUi?,
    val movers: List<CoinRowUi>,
    val coins: List<CoinRowUi>,
    val fetchedAt: Instant,
)

@Immutable
data class GlobalMarketUi(
    val totalMarketCap: String,
    val change24h: ChangeUi,
    val volume24h: String,
    val btcDominance: Float,
    val ethDominance: Float,
    val btcDominanceText: String,
    val ethDominanceText: String,
    val otherDominanceText: String,
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    observeMarketOverview: ObserveMarketOverviewUseCase,
    private val marketRepository: MarketRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val formatters = Formatters()
    private val filter = MutableStateFlow(MarketFilter.All)
    private val refresh = MutableStateFlow(RefreshState())
    private var refreshJob: Job? = null

    private val content = combine(observeMarketOverview(), filter) { overview, filter ->
        overview?.toContent(filter)
    }.flowOn(Dispatchers.Default)

    val state: StateFlow<MarketUiState> = combine(
        content,
        filter,
        refresh,
        networkMonitor.isOnline,
    ) { content, filter, refresh, online ->
        MarketUiState(
            content = when {
                content != null -> ScreenContent.Ready(content)
                refresh.lastError != null && !refresh.inFlight -> ScreenContent.Failed(refresh.lastError)
                else -> ScreenContent.Loading
            },
            filter = filter,
            isRefreshing = refresh.userInitiated && refresh.inFlight,
            syncStatus = if (content == null) SyncStatus.UpToDate else SyncStatus.from(online, refresh.lastError, content.fetchedAt),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MarketUiState())

    init {
        refresh(force = false, userInitiated = false)
        // Coming back online: refresh if (and only if) the cache has gone stale meanwhile.
        networkMonitor.isOnline.distinctUntilChanged().drop(1).filter { it }
            .onEach { refresh(force = false, userInitiated = false) }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: MarketIntent) {
        when (intent) {
            MarketIntent.Refresh -> refresh(force = true, userInitiated = true)
            MarketIntent.ScreenResumed -> refresh(force = false, userInitiated = false)
            is MarketIntent.FilterSelected -> filter.value = intent.filter
        }
    }

    private fun refresh(force: Boolean, userInitiated: Boolean) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refresh.value = refresh.value.copy(inFlight = true, userInitiated = userInitiated)
            val outcome = marketRepository.refreshMarket(force)
            refresh.value = RefreshState(lastError = (outcome as? Outcome.Failure)?.error)
        }
    }

    private fun MarketOverview.toContent(filter: MarketFilter): MarketContent {
        val row = { coin: Coin -> coin.toRowUi(formatters, coin.id in watchlist) }
        return MarketContent(
            global = global?.let { global ->
                val btc = (global.btcDominancePercent ?: 0.0).toFloat()
                val eth = (global.ethDominancePercent ?: 0.0).toFloat()
                GlobalMarketUi(
                    totalMarketCap = formatters.compactCurrency(global.totalMarketCap),
                    change24h = ChangeUi.of(global.marketCapChange24hPercent, formatters),
                    volume24h = formatters.compactCurrency(global.totalVolume24h),
                    btcDominance = btc / 100f,
                    ethDominance = eth / 100f,
                    btcDominanceText = formatters.percent(btc.toDouble(), signed = false),
                    ethDominanceText = formatters.percent(eth.toDouble(), signed = false),
                    otherDominanceText = formatters.percent((100.0 - btc - eth).coerceAtLeast(0.0), signed = false),
                )
            },
            movers = (topGainers + topLosers).map(row),
            coins = filtered(filter).map(row),
            fetchedAt = fetchedAt,
        )
    }

    private data class RefreshState(
        val inFlight: Boolean = false,
        val userInitiated: Boolean = false,
        val lastError: AppError? = null,
    )
}
