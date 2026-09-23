package com.thecode.cryptomania.data.network

import com.thecode.cryptomania.di.ApplicationScope
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Outcome
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single gateway for every CoinGecko request, protecting the free-tier budget:
 *
 * - **Deduplication**: concurrent callers asking for the same key share one in-flight request.
 * - **Rate-limit cooldown**: after an HTTP 429 no request is sent until `Retry-After` elapses
 *   (60 s by default); callers get [AppError.RateLimited] immediately and keep cached data.
 * - **Retry with backoff**: timeouts and 5xx are retried twice (1 s, then 2 s). Offline,
 *   4xx and 429 are never retried.
 *
 * Work runs in the application scope, so leaving a screen does not waste a request that is
 * already paid for: its result still lands in the cache.
 */
@Singleton
class RequestCoordinator @Inject constructor(
    private val clock: Clock,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    private val mutex = Mutex()
    private val inFlight = mutableMapOf<String, Deferred<Outcome<Any?>>>()
    private var cooldownUntil: Instant = Instant.MIN

    suspend fun <T> execute(key: String, block: suspend () -> T): Outcome<T> {
        val deferred = mutex.withLock {
            if (clock.instant().isBefore(cooldownUntil)) return Outcome.Failure(AppError.RateLimited)
            inFlight.getOrPut(key) {
                scope.async {
                    try {
                        runWithRetry(block)
                    } finally {
                        mutex.withLock { inFlight.remove(key) }
                    }
                }
            }
        }
        @Suppress("UNCHECKED_CAST")
        return deferred.await() as Outcome<T>
    }

    private suspend fun <T> runWithRetry(block: suspend () -> T): Outcome<Any?> {
        var attempt = 0
        while (true) {
            try {
                return Outcome.Success(block())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                val error = e.toAppError()
                if (error == AppError.RateLimited) {
                    val wait = e.retryAfter() ?: DEFAULT_COOLDOWN
                    mutex.withLock { cooldownUntil = clock.instant().plus(wait) }
                }
                val retryable = error == AppError.Timeout || error == AppError.ServerUnavailable
                if (!retryable || attempt >= MAX_RETRIES) return Outcome.Failure(error)
                delay(BASE_BACKOFF.multipliedBy(1L shl attempt).toMillis())
                attempt++
            }
        }
    }

    private companion object {
        const val MAX_RETRIES = 2
        val BASE_BACKOFF: Duration = Duration.ofSeconds(1)
        val DEFAULT_COOLDOWN: Duration = Duration.ofSeconds(60)
    }
}
