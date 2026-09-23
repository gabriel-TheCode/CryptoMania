package com.thecode.cryptomania.presentation.util

import com.thecode.cryptomania.domain.model.ChartRange
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10

/**
 * Locale-aware formatting of financial values. Prices get precision that matches their
 * magnitude (BTC shows cents, a meme coin shows significant digits) instead of a fixed
 * number of decimals that would either hide information or fake precision.
 */
class Formatters(
    private val locale: Locale = Locale.getDefault(),
    private val zone: ZoneId = ZoneId.systemDefault(),
) {
    private val symbols = DecimalFormatSymbols.getInstance(locale)

    fun price(value: Double?): String {
        value ?: return PLACEHOLDER
        val magnitude = abs(value)
        val (maxDecimals, minDecimals) = when {
            magnitude == 0.0 || magnitude >= 1.0 -> 2 to 2
            magnitude >= 0.01 -> 4 to 4
            // Four significant digits for sub-cent assets, capped to stay readable.
            else -> (-floor(log10(magnitude)).toInt() + 3).coerceAtMost(MAX_DECIMALS) to 2
        }
        return "$" + decimal(maxDecimals, minDecimals).format(value)
    }

    /** $2.88T, $845.30B, $12.40M, $950.20K; exact below one thousand. */
    fun compactCurrency(value: Double?): String {
        value ?: return PLACEHOLDER
        return if (abs(value) < 1_000) price(value) else "$" + compact(value)
    }

    fun compactNumber(value: Double?): String {
        value ?: return PLACEHOLDER
        return if (abs(value) < 1_000) decimal(maxDecimals = 2, minDecimals = 0).format(value) else compact(value)
    }

    /** Signed percentage, e.g. `+2.34%` / `-0.87%`; zero is unsigned. */
    fun percent(value: Double?, signed: Boolean = true): String {
        value ?: return PLACEHOLDER
        val rounded = value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
        val sign = when {
            !signed || rounded == 0.0 -> ""
            rounded > 0 -> "+"
            else -> "-"
        }
        val body = decimal(maxDecimals = 2, minDecimals = 2).format(if (signed) abs(rounded) else rounded)
        return "$sign$body%"
    }

    fun rank(rank: Int?): String = rank?.let { "#$it" } ?: PLACEHOLDER

    fun chartTimestamp(epochMillis: Long, range: ChartRange): String {
        val time = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val formatter = when (range) {
            ChartRange.OneDay -> DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            ChartRange.OneWeek, ChartRange.OneMonth, ChartRange.ThreeMonths ->
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            ChartRange.OneYear -> DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        }
        return formatter.withLocale(locale).format(time)
    }

    fun date(instant: Instant): String =
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(instant.atZone(zone))

    private fun compact(value: Double): String {
        val magnitude = abs(value)
        val (divisor, suffix) = when {
            magnitude >= 1e12 -> 1e12 to "T"
            magnitude >= 1e9 -> 1e9 to "B"
            magnitude >= 1e6 -> 1e6 to "M"
            else -> 1e3 to "K"
        }
        return decimal(maxDecimals = 2, minDecimals = 2).format(value / divisor) + suffix
    }

    private fun decimal(maxDecimals: Int, minDecimals: Int): DecimalFormat =
        DecimalFormat("#,##0", symbols).apply {
            maximumFractionDigits = maxDecimals
            minimumFractionDigits = minDecimals.coerceIn(0, maxDecimals)
            roundingMode = RoundingMode.HALF_UP
        }

    companion object {
        const val PLACEHOLDER = "—"
        private const val MAX_DECIMALS = 10
    }
}
