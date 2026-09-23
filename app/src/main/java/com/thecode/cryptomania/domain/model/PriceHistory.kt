package com.thecode.cryptomania.domain.model

import java.time.Duration

/**
 * Chart ranges exposed to the user. Limited to what the CoinGecko Demo/keyless tier serves:
 * history is capped at 365 days, and granularity is chosen by the API
 * (5-minutely for 1 day, hourly up to 90 days, daily beyond).
 */
enum class ChartRange(val days: Int, val cacheTtl: Duration) {
    OneDay(days = 1, cacheTtl = Duration.ofMinutes(5)),
    OneWeek(days = 7, cacheTtl = Duration.ofMinutes(30)),
    OneMonth(days = 30, cacheTtl = Duration.ofHours(2)),
    ThreeMonths(days = 90, cacheTtl = Duration.ofHours(6)),
    OneYear(days = 365, cacheTtl = Duration.ofHours(12)),
}

data class PricePoint(val epochMillis: Long, val price: Double)

data class PriceHistory(
    val coinId: String,
    val range: ChartRange,
    val points: List<PricePoint>,
) {
    val first: PricePoint? get() = points.firstOrNull()
    val last: PricePoint? get() = points.lastOrNull()
    val low: Double? get() = points.minOfOrNull { it.price }
    val high: Double? get() = points.maxOfOrNull { it.price }

    /** Percentage change between the first and last point, or null when it cannot be computed. */
    val changePercent: Double?
        get() {
            val start = first?.price ?: return null
            val end = last?.price ?: return null
            if (points.size < 2 || start == 0.0) return null
            return (end - start) / start * 100.0
        }
}
