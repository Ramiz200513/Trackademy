package org.example.project.core.di

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.project.data.remote.dto.LoginResponse
import org.example.project.data.remote.dto.RefreshRequest
import org.example.project.data.repository.*
import org.example.project.domain.repository.*
import org.koin.core.module.Module
import org.koin.dsl.module

val networkModule: Module = module {
    single<Settings> { Settings() }

    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }

            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
                filter { requestBuilder ->
                    requestBuilder.url.host.contains("trackademy.kz")
                }
            }

            install(Auth) {
                bearer {
                    loadTokens {
                        val settings = get<Settings>()
                        val token = settings.getString("token", "")
                        val refresh = settings.getString("refreshToken", "")
                        if (token.isNotEmpty()) BearerTokens(token, refresh) else null
                    }

                    refreshTokens {
                        val settings = get<Settings>()
                        val refreshToken = settings.getString("refreshToken", "")

                        if (refreshToken.isEmpty()) return@refreshTokens null

                        try {
                            val refreshResponse = client.post("api/Auth/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(RefreshRequest(refreshToken))
                                markAsRefreshTokenRequest()
                            }

                            if (refreshResponse.status.value in 200..299) {
                                val newAuthData = refreshResponse.body<LoginResponse>()

                                settings.putString("token", newAuthData.token)
                                if (newAuthData.refreshToken != null) {
                                    settings.putString("refreshToken", newAuthData.refreshToken)
                                }

                                println("♻️ ТОКЕН УСПЕШНО ОБНОВЛЕН!")
                                BearerTokens(newAuthData.token, newAuthData.refreshToken ?: "")
                            } else {
                                println("❌ REFRESH TOKEN ПРОТУХ, ТРЕБУЕТСЯ ПОВТОРНЫЙ ЛОГИН")
                                settings.remove("token")
                                settings.remove("refreshToken")
                                null
                            }
                        } catch (e: Exception) {
                            println("❌ ОШИБКА ПРИ ОБНОВЛЕНИИ ТОКЕНА: ${e.message}")
                            null
                        }
                    }
                }
            }

            defaultRequest {
                url("https://dev.trackademy.kz/")
            }
        }
    }
}

val repositoryModule: Module = module {
    single<AuthRepository> { AuthRepositoryImpl(httpClient = get(), settings = get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(httpClient = get(), settings = get()) }
    single<TeacherRepository> { TeacherRepositoryImpl(get(), get()) }
    single<MaterialRepository> { MaterialRepositoryImpl(httpClient = get(), settings = get()) }
    single<AssignmentRepository> { AssignmentRepositoryImpl(httpClient = get(), settings = get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<ScheduleRepository> { ScheduleRepositoryImpl(httpClient = get(), settings = get()) }
    single<PerformanceRepository> { PerformanceRepositoryImpl(httpClient = get(), settings = get()) }
}

val appModules: List<Module> = listOf(networkModule, repositoryModule)