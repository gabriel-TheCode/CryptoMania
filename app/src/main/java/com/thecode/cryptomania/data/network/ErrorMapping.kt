package com.thecode.cryptomania.data.network

import com.thecode.cryptomania.domain.model.AppError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import java.time.Duration

/** Translates transport-level failures into [AppError]. The only place that knows about HTTP. */
fun Throwable.toAppError(): AppError = when (this) {
    is HttpException -> when (code()) {
        401, 403 -> AppError.Unauthorized
        404 -> AppError.NotFound
        408 -> AppError.Timeout
        429 -> AppError.RateLimited
        in 500..599 -> AppError.ServerUnavailable
        else -> AppError.Unknown(this)
    }
    // SocketTimeoutException is an InterruptedIOException.
    is InterruptedIOException -> AppError.Timeout
    is UnknownHostException, is ConnectException, is NoRouteToHostException -> AppError.NetworkUnavailable
    is IOException -> AppError.NetworkUnavailable
    is SerializationException -> AppError.Unknown(this)
    else -> AppError.Unknown(this)
}

/** `Retry-After` in seconds, as sent by CoinGecko on HTTP 429. */
fun Throwable.retryAfter(): Duration? =
    (this as? HttpException)?.response()?.headers()?.get("Retry-After")?.trim()?.toLongOrNull()
        ?.takeIf { it > 0 }
        ?.let(Duration::ofSeconds)
