package com.thecode.cryptomania.data.repository

import java.time.Clock
import java.time.Duration

/**
 * Freshness rules for every cached resource, in one place.
 *
 * CoinGecko itself caches `/coins/markets` for 60 s on the free tier, so refreshing faster
 * only burns quota for identical data. Even a forced refresh (pull-to-refresh) is ignored
 * when the cache is younger than [MIN_FORCED_REFRESH_INTERVAL].
 */
object CachePolicy {
    val MARKET_TTL: Duration = Duration.ofMinutes(2)
    val GLOBAL_TTL: Duration = Duration.ofMinutes(5)
    val PROFILE_TTL: Duration = Duration.ofDays(1)
    val EXCHANGES_TTL: Duration = Duration.ofHours(1)
    /** CoinGecko recomputes trending coins roughly every 10 minutes. */
    val TRENDING_TTL: Duration = Duration.ofMinutes(15)
    val MIN_FORCED_REFRESH_INTERVAL: Duration = Duration.ofSeconds(30)

    /** Charts and search-only coins older than this are evicted to keep the database small. */
    val EVICTION_AGE: Duration = Duration.ofDays(7)

    fun shouldRefresh(fetchedAtMillis: Long?, ttl: Duration, force: Boolean, clock: Clock): Boolean {
        if (fetchedAtMillis == null) return true
        val age = clock.millis() - fetchedAtMillis
        if (age < 0) return true // Device clock moved backwards: do not trust the cache age.
        val threshold = if (force) MIN_FORCED_REFRESH_INTERVAL else ttl
        return age >= threshold.toMillis()
    }
}
