package com.thecode.cryptomania.presentation.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalResources
import com.thecode.cryptomania.R
import com.thecode.cryptomania.domain.model.AppError
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

/**
 * What a screen can show for its primary content. Loading and Failed only happen when there
 * is nothing cached; once data exists the screen stays [Ready] and problems surface through
 * [SyncStatus] instead of replacing useful content with an error page.
 */
@Immutable
sealed interface ScreenContent<out T> {
    data object Loading : ScreenContent<Nothing>
    data class Failed(val error: AppError) : ScreenContent<Nothing>
    data class Ready<out T>(val data: T) : ScreenContent<T>
}

/** Freshness of the data on screen, shown as a subtle banner. */
@Immutable
sealed interface SyncStatus {
    data object UpToDate : SyncStatus
    data class Offline(val dataFrom: Instant?) : SyncStatus
    data class Degraded(val error: AppError, val dataFrom: Instant?) : SyncStatus

    companion object {
        /**
         * Connectivity is only a hint: a failed request while "online" still degrades, and
         * a stale cache while offline is reported as offline with its age.
         */
        fun from(isOnline: Boolean, lastError: AppError?, dataFrom: Instant?): SyncStatus = when {
            !isOnline -> Offline(dataFrom)
            lastError != null -> Degraded(lastError, dataFrom)
            else -> UpToDate
        }
    }
}

@StringRes
fun AppError.titleRes(): Int = when (this) {
    AppError.NetworkUnavailable -> R.string.error_network_title
    AppError.Timeout -> R.string.error_timeout_title
    AppError.RateLimited -> R.string.error_rate_limited_title
    AppError.ServerUnavailable -> R.string.error_server_title
    AppError.Unauthorized -> R.string.error_unauthorized_title
    AppError.NotFound -> R.string.error_not_found_title
    is AppError.Unknown -> R.string.error_unknown_title
}

@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.NetworkUnavailable -> R.string.error_network_message
    AppError.Timeout -> R.string.error_timeout_message
    AppError.RateLimited -> R.string.error_rate_limited_message
    AppError.ServerUnavailable -> R.string.error_server_message
    AppError.Unauthorized -> R.string.error_unauthorized_message
    AppError.NotFound -> R.string.error_not_found_message
    is AppError.Unknown -> R.string.error_unknown_message
}

@StringRes
fun AppError.shortRes(): Int = when (this) {
    AppError.NetworkUnavailable -> R.string.error_short_network
    AppError.Timeout -> R.string.error_short_timeout
    AppError.RateLimited -> R.string.error_short_rate_limited
    AppError.ServerUnavailable -> R.string.error_short_server
    AppError.Unauthorized -> R.string.error_short_unauthorized
    AppError.NotFound, is AppError.Unknown -> R.string.error_short_unknown
}

/** "just now", "5 min ago", ... re-evaluated every 30 s while on screen. */
@Composable
fun rememberRelativeTime(instant: Instant): String {
    val resources = LocalResources.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(instant) {
        while (true) {
            now = System.currentTimeMillis()
            delay(30_000)
        }
    }
    val elapsed = Duration.ofMillis((now - instant.toEpochMilli()).coerceAtLeast(0))
    return when {
        elapsed.toMinutes() < 1 -> resources.getString(R.string.time_just_now)
        elapsed.toHours() < 1 -> elapsed.toMinutes().toInt().let { resources.getQuantityString(R.plurals.time_minutes_ago, it, it) }
        elapsed.toDays() < 1 -> elapsed.toHours().toInt().let { resources.getQuantityString(R.plurals.time_hours_ago, it, it) }
        else -> elapsed.toDays().toInt().let { resources.getQuantityString(R.plurals.time_days_ago, it, it) }
    }
}
