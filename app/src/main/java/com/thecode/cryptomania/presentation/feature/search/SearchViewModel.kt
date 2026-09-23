package com.thecode.cryptomania.presentation.feature.search

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.CoinSearchHit
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.repository.MarketRepository
import com.thecode.cryptomania.domain.repository.SettingsRepository
import com.thecode.cryptomania.domain.usecase.RankLocalSearchUseCase
import com.thecode.cryptomania.presentation.model.CoinRowUi
import com.thecode.cryptomania.presentation.model.toRowUi
import com.thecode.cryptomania.presentation.util.Formatters
import dagger.hilt.android.lifecycle.HiltViewModel
import com.thecode.cryptomania.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent
    data object Submit : SearchIntent
    data object ClearRecent : SearchIntent
    data class ResultSelected(val coinId: String) : SearchIntent
}

sealed interface SearchEffect {
    data class OpenCoin(val coinId: String) : SearchEffect
}

@Immutable
sealed interface RemoteResults {
    data object Idle : RemoteResults
    data object Loading : RemoteResults
    data class Loaded(val hits: List<SearchHitUi>) : RemoteResults
    data class Failed(val error: AppError) : RemoteResults
}

@Immutable
data class SearchHitUi(val id: String, val name: String, val symbol: String, val thumbUrl: String?, val rank: String?)

@Immutable
data class SearchUiState(
    val query: String = "",
    val recentSearches: List<String> = emptyList(),
    val suggestions: List<CoinRowUi> = emptyList(),
    val localResults: List<CoinRowUi> = emptyList(),
    val remote: RemoteResults = RemoteResults.Idle,
) {
    val showNoResults: Boolean
        get() = query.isNotBlank() && localResults.isEmpty() &&
            (remote is RemoteResults.Loaded && remote.hits.isEmpty() || remote is RemoteResults.Failed)
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
    private val rankLocalSearch: RankLocalSearchUseCase,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val formatters = Formatters()
    private val query = MutableStateFlow("")
    /** Bumped on IME "search" to force a remote lookup even when local results look sufficient. */
    private val submitted = MutableStateFlow<String?>(null)
    private val effects = Channel<SearchEffect>(Channel.BUFFERED)
    val effect: Flow<SearchEffect> = effects.receiveAsFlow()

    // Shared: both suggestions and local search read the same cached list from one Room query.
    private val cachedCoins = marketRepository.observeTopCoins()
        .map { it?.value.orEmpty() }
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    private val localResults = combine(query.debounce(LOCAL_DEBOUNCE_MS), cachedCoins) { q, coins ->
        q to rankLocalSearch(coins, q).map { it.toRowUi(formatters, isWatched = false) }
    }.flowOn(defaultDispatcher)
        .shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), replay = 1)

    /**
     * The network is only consulted when the local cache (top 250 coins) cannot answer:
     * after the user stops typing for [REMOTE_DEBOUNCE_MS], with at least two characters, and
     * either few local matches or an explicit submit.
     */
    private val remoteResults: Flow<RemoteResults> = combine(
        localResults.debounce(REMOTE_DEBOUNCE_MS - LOCAL_DEBOUNCE_MS),
        submitted,
    ) { (q, local), submit -> Triple(q.trim(), local, submit) }
        .distinctUntilChanged()
        .transformLatest { (q, local, submit) ->
            val wanted = q.length >= MIN_REMOTE_QUERY && (local.size < LOCAL_SUFFICIENT || submit == q)
            if (!wanted) {
                emit(RemoteResults.Idle)
                return@transformLatest
            }
            emit(RemoteResults.Loading)
            val localIds = local.mapTo(HashSet()) { it.id }
            emit(
                when (val outcome = marketRepository.searchRemote(q)) {
                    is Outcome.Success -> RemoteResults.Loaded(
                        outcome.value.filterNot { it.id in localIds }.take(MAX_REMOTE_RESULTS).map { it.toUi() },
                    )
                    is Outcome.Failure -> RemoteResults.Failed(outcome.error)
                },
            )
        }
        .onStart { emit(RemoteResults.Idle) }

    private val suggestions = cachedCoins.map { coins ->
        coins.take(SUGGESTIONS).map { it.toRowUi(formatters, isWatched = false) }
    }.flowOn(defaultDispatcher)

    val state: StateFlow<SearchUiState> = combine(
        query,
        settingsRepository.recentSearches,
        suggestions,
        localResults,
        remoteResults,
    ) { query, recent, suggestions, (localQuery, local), remote ->
        SearchUiState(
            query = query,
            recentSearches = recent,
            suggestions = suggestions,
            // Hide results computed for an older query until the debounce catches up.
            localResults = if (localQuery == query) local else emptyList(),
            remote = if (query.isBlank()) RemoteResults.Idle else remote,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchUiState())

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> query.value = intent.query.take(MAX_QUERY_LENGTH)
            SearchIntent.Submit -> {
                val q = query.value.trim()
                if (q.isNotEmpty()) {
                    submitted.value = q
                    viewModelScope.launch { settingsRepository.addRecentSearch(q) }
                }
            }
            SearchIntent.ClearRecent -> viewModelScope.launch { settingsRepository.clearRecentSearches() }
            is SearchIntent.ResultSelected -> viewModelScope.launch {
                query.value.trim().takeIf { it.isNotEmpty() }?.let { settingsRepository.addRecentSearch(it) }
                effects.send(SearchEffect.OpenCoin(intent.coinId))
            }
        }
    }

    private fun CoinSearchHit.toUi() = SearchHitUi(id, name, symbol, thumbUrl, marketCapRank?.let(formatters::rank))

    companion object {
        const val LOCAL_DEBOUNCE_MS = 120L
        const val REMOTE_DEBOUNCE_MS = 500L
        private const val MIN_REMOTE_QUERY = 2
        private const val LOCAL_SUFFICIENT = 5
        private const val MAX_REMOTE_RESULTS = 20
        private const val SUGGESTIONS = 10
        private const val MAX_QUERY_LENGTH = 64
    }
}
