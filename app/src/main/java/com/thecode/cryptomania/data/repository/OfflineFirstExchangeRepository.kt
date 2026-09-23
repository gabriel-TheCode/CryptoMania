package com.thecode.cryptomania.data.repository

import com.thecode.cryptomania.data.local.dao.ExchangeDao
import com.thecode.cryptomania.data.mapper.toDomain
import com.thecode.cryptomania.data.mapper.toEntity
import com.thecode.cryptomania.data.network.RequestCoordinator
import com.thecode.cryptomania.data.remote.CoinGeckoApi
import com.thecode.cryptomania.domain.model.Cached
import com.thecode.cryptomania.domain.model.Done
import com.thecode.cryptomania.domain.model.Exchange
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.domain.repository.ExchangeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstExchangeRepository @Inject constructor(
    private val api: CoinGeckoApi,
    private val exchangeDao: ExchangeDao,
    private val coordinator: RequestCoordinator,
    private val clock: Clock,
) : ExchangeRepository {

    override fun observeExchanges(): Flow<Cached<List<Exchange>>?> = exchangeDao.observeAll().map { entities ->
        if (entities.isEmpty()) {
            null
        } else {
            Cached(entities.map { it.toDomain() }, Instant.ofEpochMilli(entities.maxOf { it.fetchedAtMillis }))
        }
    }

    override suspend fun refreshExchanges(force: Boolean): Outcome<Unit> {
        if (!CachePolicy.shouldRefresh(exchangeDao.fetchedAt(), CachePolicy.EXCHANGES_TTL, force, clock)) return Done
        return coordinator.execute("exchanges") {
            val now = clock.millis()
            exchangeDao.replaceAll(api.exchanges().mapIndexed { index, dto -> dto.toEntity(index, now) })
        }
    }
}
