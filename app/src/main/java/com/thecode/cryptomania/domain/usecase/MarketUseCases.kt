package com.thecode.cryptomania.domain.usecase

import com.thecode.cryptomania.domain.model.Coin
import com.thecode.cryptomania.domain.model.GlobalMarket
import com.thecode.cryptomania.domain.repository.MarketRepository
import com.thecode.cryptomania.domain.repository.WatchlistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

enum class MarketFilter { All, Watchlist, Gainers, Losers }

data class MarketOverview(
    val coins: List<Coin>,
    val global: GlobalMarket?,
    val watchlist: Set<String>,
    /** Watched coins, including ones outside the top list, ordered by market cap rank. */
    val watchedCoins: List<Coin>,
    val fetchedAt: Instant,
) {
    /** Movers are picked among established assets so illiquid micro-caps do not dominate. */
    val topGainers: List<Coin> by lazy {
        moverCandidates.filter { (it.change24hPercent ?: 0.0) > 0 }.sortedByDescending { it.change24hPercent }.take(MOVERS)
    }
    val topLosers: List<Coin> by lazy {
        moverCandidates.filter { (it.change24hPercent ?: 0.0) < 0 }.sortedBy { it.change24hPercent }.take(MOVERS)
    }

    private val moverCandidates: List<Coin>
        get() = coins.filter { it.change24hPercent != null && (it.marketCapRank ?: Int.MAX_VALUE) <= MOVER_RANK_LIMIT }

    fun filtered(filter: MarketFilter): List<Coin> = when (filter) {
        MarketFilter.All -> coins
        MarketFilter.Watchlist -> watchedCoins
        MarketFilter.Gainers -> coins.filter { (it.change24hPercent ?: 0.0) > 0 }.sortedByDescending { it.change24hPercent }
        MarketFilter.Losers -> coins.filter { (it.change24hPercent ?: 0.0) < 0 }.sortedBy { it.change24hPercent }
    }

    private companion object {
        const val MOVERS = 5
        const val MOVER_RANK_LIMIT = 100
    }
}

/** Streams the cached market state; emits null until a first fetch has been stored. */
class ObserveMarketOverviewUseCase @Inject constructor(
    private val marketRepository: MarketRepository,
    private val watchlistRepository: WatchlistRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<MarketOverview?> {
        val watched = watchlistRepository.observeWatchlist().flatMapLatest { ids ->
            marketRepository.observeCoins(ids).map { coins ->
                ids to coins.sortedBy { it.marketCapRank ?: Int.MAX_VALUE }
            }
        }
        return combine(
            marketRepository.observeTopCoins(),
            marketRepository.observeGlobalMarket(),
            watched,
        ) { coins, global, (ids, watchedCoins) ->
            coins?.let { MarketOverview(it.value, global, ids, watchedCoins, it.fetchedAt) }
        }
    }
}

/**
 * Ranks locally cached coins against a query: exact symbol, then name/symbol prefix,
 * then substring; ties resolved by market-cap rank. Pure, so search never needs the
 * network for assets already in the cache.
 */
class RankLocalSearchUseCase @Inject constructor() {
    operator fun invoke(coins: List<Coin>, rawQuery: String, limit: Int = 50): List<Coin> {
        val query = rawQuery.trim().lowercase()
        if (query.isEmpty()) return emptyList()
        return coins.asSequence()
            .mapNotNull { coin ->
                val symbol = coin.symbol.lowercase()
                val name = coin.name.lowercase()
                val score = when {
                    symbol == query -> 0
                    name == query -> 1
                    symbol.startsWith(query) -> 2
                    name.startsWith(query) -> 3
                    name.split(' ', '-').any { it.startsWith(query) } -> 4
                    query.length >= 2 && (name.contains(query) || symbol.contains(query)) -> 5
                    else -> return@mapNotNull null
                }
                coin to score
            }
            .sortedWith(compareBy({ it.second }, { it.first.marketCapRank ?: Int.MAX_VALUE }))
            .take(limit)
            .map { it.first }
            .toList()
    }
}
