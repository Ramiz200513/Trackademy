package org.example.project.core.network

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import org.example.project.core.domain.NetworkError
import org.example.project.core.logging.AppLogger

/** Thrown by [ensureSuccess] when the server responds with a non-2xx status. */
class HttpStatusException(val status: HttpStatusCode) :
    Exception("HTTP ${status.value} ${status.description}")

/** Returns the response if it is 2xx, otherwise throws [HttpStatusException]. */
fun HttpResponse.ensureSuccess(): HttpResponse {
    if (!status.isSuccess()) throw HttpStatusException(status)
    return this
}

/** Maps an HTTP status code to a domain [NetworkError]. */
fun HttpStatusCode.toNetworkError(): NetworkError = when (value) {
    400, 422 -> NetworkError.BadRequest
    401 -> NetworkError.Unauthorized
    403 -> NetworkError.Forbidden
    404 -> NetworkError.NotFound
    408 -> NetworkError.RequestTimeout
    in 500..599 -> NetworkError.ServerError
    else -> NetworkError.Unknown
}

/**
 * Maps a thrown exception to a domain [NetworkError].
 *
 * [CancellationException] is rethrown: swallowing it would break structured
 * concurrency (a cancelled request would look like a failure).
 */
fun Throwable.toNetworkError(): NetworkError {
    if (this is CancellationException) throw this
    return when (this) {
        is HttpRequestTimeoutException,
        is ConnectTimeoutException,
        is SocketTimeoutException -> NetworkError.RequestTimeout
        is HttpStatusException -> status.toNetworkError()
        is ResponseException -> response.status.toNetworkError()
        is SerializationException,
        is ContentConvertException -> NetworkError.Serialization
        is IOException -> NetworkError.NoInternet
        else -> NetworkError.Unknown
    }
}

/**
 * Executes a network [block] and wraps the outcome into [Result].
 * Any failure is logged (debug builds only) and mapped to [NetworkError].
 */
suspend inline fun <T> safeApiCall(
    tag: String = "Network",
    crossinline block: suspend () -> T
): Result<T, NetworkError> = try {
    Result.Success(block())
} catch (e: Throwable) {
    val error = e.toNetworkError()
    AppLogger.e(tag, "Request failed: $error", e)
    Result.Error(error)
}
