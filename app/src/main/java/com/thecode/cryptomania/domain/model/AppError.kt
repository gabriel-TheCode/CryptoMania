package com.thecode.cryptomania.domain.model

import java.time.Instant

/** Application-level failures. Raw HTTP/IO exceptions never leave the data layer. */
sealed interface AppError {
    data object NetworkUnavailable : AppError
    data object Timeout : AppError
    data object RateLimited : AppError
    data object ServerUnavailable : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data class Unknown(val cause: Throwable? = null) : AppError
}

/**
 * Result of a network-backed operation. For refreshes the value is [Unit]: the data itself
 * always flows from the local cache.
 */
sealed interface Outcome<out T> {
    data class Success<out T>(val value: T) : Outcome<T>
    data class Failure(val error: AppError) : Outcome<Nothing>
}

val Done: Outcome<Unit> = Outcome.Success(Unit)

/** A cached value together with the moment it was fetched from the network. */
data class Cached<out T>(val value: T, val fetchedAt: Instant)
