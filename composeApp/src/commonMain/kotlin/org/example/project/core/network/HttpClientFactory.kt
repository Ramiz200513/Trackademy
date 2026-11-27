package org.example.project.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.plugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.core.config.AppConfig
import org.example.project.core.logging.AppLogger
import org.example.project.core.session.SessionStorage
import org.example.project.data.remote.dto.LoginResponse
import org.example.project.data.remote.dto.RefreshRequest

/** Shared JSON configuration for the whole app. */
val AppJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private const val TAG = "HttpClient"
private const val REQUEST_TIMEOUT_MS = 30_000L
private const val CONNECT_TIMEOUT_MS = 15_000L

/**
 * Builds the app [HttpClient]: JSON, timeouts, bearer auth with automatic
 * token refresh and debug-only logging with a sanitized Authorization header.
 *
 * @param engine optional engine, used by tests to plug in a MockEngine.
 */
fun createHttpClient(
    session: SessionStorage,
    baseUrl: String = AppConfig.apiBaseUrl,
    enableLogging: Boolean = AppConfig.isDebug,
    engine: HttpClientEngine? = null
): HttpClient {
    val config: HttpClientConfig<*>.() -> Unit = {
        expectSuccess = false

        install(ContentNegotiation) { json(AppJson) }

        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MS
            connectTimeoutMillis = CONNECT_TIMEOUT_MS
            socketTimeoutMillis = REQUEST_TIMEOUT_MS
        }

        if (enableLogging) {
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) = AppLogger.d(TAG, message)
                }
                level = LogLevel.ALL
                sanitizeHeader { it == HttpHeaders.Authorization }
            }
        }

        install(Auth) {
            bearer {
                loadTokens {
                    session.accessToken?.let { BearerTokens(it, session.refreshToken.orEmpty()) }
                }
                refreshTokens {
                    val refreshToken = session.refreshToken ?: return@refreshTokens null
                    try {
                        val response = client.post("api/Auth/refresh") {
                            contentType(ContentType.Application.Json)
                            setBody(RefreshRequest(refreshToken))
                            markAsRefreshTokenRequest()
                        }
                        if (response.status.isSuccess()) {
                            val auth = response.body<LoginResponse>()
                            session.saveTokens(auth.token, auth.refreshToken)
                            AppLogger.i(TAG, "Access token refreshed")
                            BearerTokens(auth.token, auth.refreshToken ?: refreshToken)
                        } else {
                            AppLogger.i(TAG, "Refresh token expired, re-login required")
                            session.clearTokens()
                            null
                        }
                    } catch (e: Throwable) {
                        e.toNetworkError() // rethrows CancellationException
                        AppLogger.e(TAG, "Token refresh failed", e)
                        null
                    }
                }
            }
        }

        defaultRequest { url(baseUrl) }
    }
    return if (engine != null) HttpClient(engine, config) else HttpClient(config)
}

/** Drops tokens cached by the Auth plugin (call on logout). */
fun HttpClient.clearAuthTokens() {
    plugin(Auth).providers.filterIsInstance<BearerAuthProvider>().forEach { it.clearToken() }
}
