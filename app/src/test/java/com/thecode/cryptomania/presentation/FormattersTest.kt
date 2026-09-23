package com.thecode.cryptomania.presentation

import com.thecode.cryptomania.domain.model.ChartRange
import com.thecode.cryptomania.presentation.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

class FormattersTest {
    private val formatters = Formatters(Locale.US, ZoneOffset.UTC)

    @Test
    fun `price precision follows magnitude`() {
        assertEquals("$84,456.00", formatters.price(84_456.0))
        assertEquals("$1.50", formatters.price(1.5))
        assertEquals("$0.1505", formatters.price(0.1505))
        assertEquals("$0.000004452", formatters.price(0.0000044523))
        assertEquals("$0.00", formatters.price(0.0))
        assertEquals(Formatters.PLACEHOLDER, formatters.price(null))
    }

    @Test
    fun `large values are compacted`() {
        assertEquals("$2.88T", formatters.compactCurrency(2.88e12))
        assertEquals("$845.30B", formatters.compactCurrency(845.3e9))
        assertEquals("$12.40M", formatters.compactCurrency(12.4e6))
        assertEquals("$999.00", formatters.compactCurrency(999.0))
        assertEquals("21.00M", formatters.compactNumber(21_000_000.0))
    }

    @Test
    fun `percentages are signed and rounded`() {
        assertEquals("+2.34%", formatters.percent(2.3449))
        assertEquals("-0.87%", formatters.percent(-0.8651))
        assertEquals("0.00%", formatters.percent(-0.001))
        assertEquals("58.76%", formatters.percent(58.76, signed = false))
    }

    @Test
    fun `other locales use their own separators`() {
        assertEquals("$84.456,00", Formatters(Locale.GERMANY).price(84_456.0))
    }

    @Test
    fun `chart timestamps match the range granularity`() {
        val noon = Instant.parse("2026-09-23T12:05:00Z").toEpochMilli()
        assertTrue(formatters.chartTimestamp(noon, ChartRange.OneDay).startsWith("12:05"))
        assertEquals("Sep 23, 2026", formatters.chartTimestamp(noon, ChartRange.OneYear))
    }
}
