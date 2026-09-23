package com.thecode.cryptomania.presentation.feature.exchanges

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Exchange
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.repository.ExchangeRepository
import com.thecode.cryptomania.domain.repository.MarketRepository
import com.thecode.cryptomania.domain.repository.NetworkMonitor
import com.thecode.cryptomania.presentation.util.Formatters
import com.thecode.cryptomania.presentation.util.ScreenContent
import com.thecode.cryptomania.presentation.util.SyncStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thecode.cryptomania.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface ExchangesIntent {
    data object Refresh : ExchangesIntent
    data object ScreenResumed : ExchangesIntent
}

@Immutable
data class ExchangeUi(
    val id: String,
    val name: String,
    val logoUrl: String?,
    val rank: String,
    val subtitle: String,
    val trustScore: Int?,
    val volume: String,
    val websiteUrl: String?,
)

@Immutable
data class ExchangesContent(val exchanges: List<ExchangeUi>, val fetchedAt: Instant)

@Immutable
data class ExchangesUiState(
    val content: ScreenContent<ExchangesContent> = ScreenContent.Loading,
    val isRefreshing: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.UpToDate,
)

@HiltViewModel
class ExchangesViewModel @Inject constructor(
    private val exchangeRepository: ExchangeRepository,
    marketRepository: MarketRepository,
    networkMonitor: NetworkMonitor,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val formatters = Formatters()
    private val refresh = MutableStateFlow(RefreshState())
    private var refreshJob: Job? = null

    /** CoinGecko reports exchange volume in BTC; converted with the cached BTC price, no extra call. */
    private val btcPrice = marketRepository.observeCoin(BITCOIN_ID).map { it?.price }.distinctUntilChanged()

    private val content = combine(exchangeRepository.observeExchanges(), btcPrice) { cached, btc ->
        cached?.let { ExchangesContent(it.value.map { exchange -> exchange.toUi(btc) }, it.fetchedAt) }
    }.flowOn(defaultDispatcher)

    val state: StateFlow<ExchangesUiState> = combine(content, refresh, networkMonitor.isOnline) { content, refresh, online ->
        ExchangesUiState(
            content = when {
                content != null -> ScreenContent.Ready(content)
                refresh.lastError != null && !refresh.inFlight -> ScreenContent.Failed(refresh.lastError)
                else -> ScreenContent.Loading
            },
            isRefreshing = refresh.userInitiated && refresh.inFlight,
            syncStatus = if (content == null) SyncStatus.UpToDate else SyncStatus.from(online, refresh.lastError, content.fetchedAt),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExchangesUiState())

    init {
        refresh(force = false, userInitiated = false)
        networkMonitor.isOnline.distinctUntilChanged().drop(1).filter { it }
            .onEach { refresh(force = false, userInitiated = false) }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: ExchangesIntent) {
        when (intent) {
            ExchangesIntent.Refresh -> refresh(force = true, userInitiated = true)
            ExchangesIntent.ScreenResumed -> refresh(force = false, userInitiated = false)
        }
    }

    private fun refresh(force: Boolean, userInitiated: Boolean) {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refresh.value = refresh.value.copy(inFlight = true, userInitiated = userInitiated)
            val outcome = exchangeRepository.refreshExchanges(force)
            refresh.value = RefreshState(lastError = (outcome as? Outcome.Failure)?.error)
        }
    }

    private fun Exchange.toUi(btcPrice: Double?) = ExchangeUi(
        id = id,
        name = name,
        logoUrl = imageUrl,
        rank = trustScoreRank?.toString() ?: Formatters.PLACEHOLDER,
        subtitle = listOfNotNull(country, yearEstablished?.toString()).joinToString(" · "),
        trustScore = trustScore,
        volume = when {
            volume24hBtc == null -> Formatters.PLACEHOLDER
            btcPrice != null -> "≈ " + formatters.compactCurrency(volume24hBtc * btcPrice)
            else -> "₿ " + formatters.compactNumber(volume24hBtc)
        },
        websiteUrl = websiteUrl,
    )

    private data class RefreshState(
        val inFlight: Boolean = false,
        val userInitiated: Boolean = false,
        val lastError: AppError? = null,
    )

    private companion object {
        const val BITCOIN_ID = "bitcoin"
    }
}
