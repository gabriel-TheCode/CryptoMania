package com.thecode.cryptomania.data

import com.thecode.cryptomania.data.repository.CachePolicy
import com.thecode.cryptomania.testutil.MutableClock
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

class CachePolicyTest {
    private val clock = MutableClock()
    private val now get() = clock.millis()

    @Test
    fun `empty cache always refreshes`() {
        assertTrue(CachePolicy.shouldRefresh(null, CachePolicy.MARKET_TTL, force = false, clock = clock))
    }

    @Test
    fun `fresh cache is served without a request`() {
        assertFalse(CachePolicy.shouldRefresh(now - 60_000, CachePolicy.MARKET_TTL, force = false, clock = clock))
        assertTrue(CachePolicy.shouldRefresh(now - Duration.ofMinutes(3).toMillis(), CachePolicy.MARKET_TTL, force = false, clock = clock))
    }

    @Test
    fun `forced refresh is ignored inside the minimum interval`() {
        assertFalse(CachePolicy.shouldRefresh(now - 10_000, CachePolicy.MARKET_TTL, force = true, clock = clock))
        assertTrue(CachePolicy.shouldRefresh(now - 31_000, CachePolicy.MARKET_TTL, force = true, clock = clock))
    }

    @Test
    fun `timestamps from the future are not trusted`() {
        assertTrue(CachePolicy.shouldRefresh(now + 60_000, CachePolicy.MARKET_TTL, force = false, clock = clock))
    }
}
