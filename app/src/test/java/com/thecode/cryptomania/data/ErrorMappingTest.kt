package com.thecode.cryptomania.data

import com.thecode.cryptomania.data.network.retryAfter
import com.thecode.cryptomania.data.network.toAppError
import com.thecode.cryptomania.domain.model.AppError
import kotlinx.serialization.SerializationException
import okhttp3.Headers.Companion.headersOf
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration

class ErrorMappingTest {

    private fun http(code: Int, headers: Map<String, String> = emptyMap()) = HttpException(
        Response.error<Unit>(
            "{}".toResponseBody(),
            okhttp3.Response.Builder()
                .code(code)
                .message("x")
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .headers(headersOf(*headers.flatMap { listOf(it.key, it.value) }.toTypedArray()))
                .request(okhttp3.Request.Builder().url("https://example.com/").build())
                .build(),
        ),
    )

    @Test
    fun `http status codes map to domain errors`() {
        assertEquals(AppError.Unauthorized, http(401).toAppError())
        assertEquals(AppError.Unauthorized, http(403).toAppError())
        assertEquals(AppError.NotFound, http(404).toAppError())
        assertEquals(AppError.Timeout, http(408).toAppError())
        assertEquals(AppError.RateLimited, http(429).toAppError())
        assertEquals(AppError.ServerUnavailable, http(500).toAppError())
        assertEquals(AppError.ServerUnavailable, http(503).toAppError())
        assertTrue(http(400).toAppError() is AppError.Unknown)
    }

    @Test
    fun `transport failures map to connectivity errors`() {
        assertEquals(AppError.Timeout, SocketTimeoutException().toAppError())
        assertEquals(AppError.NetworkUnavailable, UnknownHostException().toAppError())
        assertEquals(AppError.NetworkUnavailable, IOException("reset").toAppError())
        assertTrue(SerializationException("bad json").toAppError() is AppError.Unknown)
    }

    @Test
    fun `retry-after header is honoured only when valid`() {
        assertEquals(Duration.ofSeconds(42), http(429, mapOf("Retry-After" to "42")).retryAfter())
        assertNull(http(429, mapOf("Retry-After" to "soon")).retryAfter())
        assertNull(http(429).retryAfter())
        assertNull(IOException().retryAfter())
    }
}
