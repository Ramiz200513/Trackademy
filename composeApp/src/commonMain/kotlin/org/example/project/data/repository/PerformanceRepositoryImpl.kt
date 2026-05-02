package org.example.project.data.repository

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.serialization.json.Json
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.data.mapper.toDomain
import org.example.project.data.remote.dto.PerformanceDto
import org.example.project.domain.models.Performance
import org.example.project.domain.repository.PerformanceRepository


class PerformanceRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : PerformanceRepository {

    override suspend fun getPerformanceData(): Result<Performance, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val userRole = settings.getInt("userRole", 0)
            val endpoint = if (userRole == 1) "api/Dashboard/student" else "api/Dashboard/teacher"

            println("DEBUG: Используем токен: ${token.take(10)}...")
            println("DEBUG: Role=$userRole, endpoint=$endpoint")

            val response = httpClient.get(endpoint) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            println("DEBUG: Статус ответа: ${response.status}")

            if (response.status.value in 200..299) {
                val bodyText = response.bodyAsText()
                println("DEBUG: Тело ответа: $bodyText")
                val json = Json { ignoreUnknownKeys = true }
                val dto = json.decodeFromString<PerformanceDto>(bodyText)
                Result.Success(dto.toDomain())
            } else {
                val bodyText = response.bodyAsText()
                println("DEBUG: Тело ошибки: $bodyText")
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            println("DEBUG: Исключение при загрузке: ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }
}