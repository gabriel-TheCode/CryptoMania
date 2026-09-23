package com.thecode.cryptomania.data

import com.thecode.cryptomania.data.network.RequestCoordinator
import com.thecode.cryptomania.domain.model.AppError
import com.thecode.cryptomania.domain.model.Outcome
import com.thecode.cryptomania.testutil.MutableClock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.time.Duration

class RequestCoordinatorTest {

    private val clock = MutableClock()
    private fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody()))

    @Test
    fun `concurrent requests for the same key share a single call`() = runTest {
        val coordinator = RequestCoordinator(clock, backgroundScope)
        val gate = CompletableDeferred<Unit>()
        var calls = 0
        val first = async { coordinator.execute("markets") { calls++; gate.await(); "data" } }
        val second = async { coordinator.execute("markets") { calls++; "other" } }
        testScheduler.advanceUntilIdle()
        gate.complete(Unit)

        assertEquals(Outcome.Success("data"), first.await())
        assertEquals(Outcome.Success("data"), second.await())
        assertEquals(1, calls)
    }

    @Test
    fun `timeouts and server errors are retried with backoff`() = runTest {
        val coordinator = RequestCoordinator(clock, backgroundScope)
        var calls = 0
        val outcome = coordinator.execute("chart") {
            calls++
            if (calls < 3) throw httpError(503)
            "ok"
        }
        assertEquals(Outcome.Success("ok"), outcome)
        assertEquals(3, calls)
    }

    @Test
    fun `client errors are not retried`() = runTest {
        val coordinator = RequestCoordinator(clock, backgroundScope)
        var calls = 0
        val outcome = coordinator.execute("coin") {
            calls++
            throw httpError(404)
        }
        assertEquals(Outcome.Failure(AppError.NotFound), outcome)
        assertEquals(1, calls)
    }

    @Test
    fun `rate limiting pauses every request until the cooldown elapses`() = runTest {
        val coordinator = RequestCoordinator(clock, backgroundScope)
        var calls = 0
        coordinator.execute("markets") {
            calls++
            throw httpError(429)
        }

        val duringCooldown = coordinator.execute("global") { calls++; "global" }
        assertEquals(Outcome.Failure(AppError.RateLimited), duringCooldown)
        assertEquals(1, calls)

        clock.advance(Duration.ofSeconds(61))
        assertEquals(Outcome.Success("global"), coordinator.execute("global") { calls++; "global" })
        assertEquals(2, calls)
    }
}
