package org.example.project.core.network

import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException
import org.example.project.core.domain.NetworkError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NetworkErrorMapperTest {

    @Test
    fun httpStatusCodesAreMappedToDomainErrors() {
        assertEquals(NetworkError.BadRequest, HttpStatusCode.BadRequest.toNetworkError())
        assertEquals(NetworkError.Unauthorized, HttpStatusCode.Unauthorized.toNetworkError())
        assertEquals(NetworkError.Forbidden, HttpStatusCode.Forbidden.toNetworkError())
        assertEquals(NetworkError.NotFound, HttpStatusCode.NotFound.toNetworkError())
        assertEquals(NetworkError.RequestTimeout, HttpStatusCode.RequestTimeout.toNetworkError())
        assertEquals(NetworkError.ServerError, HttpStatusCode.InternalServerError.toNetworkError())
        assertEquals(NetworkError.ServerError, HttpStatusCode.BadGateway.toNetworkError())
        assertEquals(NetworkError.Unknown, HttpStatusCode(418, "Teapot").toNetworkError())
    }

    @Test
    fun exceptionsAreMappedToDomainErrors() {
        assertEquals(NetworkError.NoInternet, IOException("no route").toNetworkError())
        assertEquals(NetworkError.Serialization, SerializationException("bad json").toNetworkError())
        assertEquals(
            NetworkError.Unauthorized,
            HttpStatusException(HttpStatusCode.Unauthorized).toNetworkError()
        )
        assertEquals(NetworkError.Unknown, IllegalStateException("boom").toNetworkError())
    }

    @Test
    fun cancellationIsRethrownNotConvertedToAnError() {
        assertFailsWith<CancellationException> {
            CancellationException("cancelled").toNetworkError()
        }
    }

    @Test
    fun safeApiCallWrapsSuccessAndFailure() = runTest {
        assertEquals(Result.Success(42), safeApiCall { 42 })
        assertEquals(
            Result.Error(NetworkError.NoInternet),
            safeApiCall<Int> { throw IOException("offline") }
        )
    }

    @Test
    fun safeApiCallPropagatesCancellation() = runTest {
        assertFailsWith<CancellationException> {
            safeApiCall<Int> { throw CancellationException("cancelled") }
        }
    }
}
