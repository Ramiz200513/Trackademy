package org.example.project.data.repository

import org.example.project.core.logging.AppLogger
import org.example.project.core.network.toNetworkError

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
            val userRole = settings.getInt("userRole", 0)
            val endpoint = if (userRole == 1) "api/Dashboard/student" else "api/Dashboard/teacher"

            AppLogger.d("PerformanceRepository", "DEBUG: Role=$userRole, endpoint=$endpoint")

            val response = httpClient.get(endpoint)

            AppLogger.d("PerformanceRepository", "DEBUG: Статус ответа: ${response.status}")

            if (response.status.value in 200..299) {
                val bodyText = response.bodyAsText()
                AppLogger.d("PerformanceRepository", "DEBUG: Тело ответа: $bodyText")
                val json = Json { ignoreUnknownKeys = true }
                val dto = json.decodeFromString<PerformanceDto>(bodyText)
                Result.Success(dto.toDomain())
            } else {
                val bodyText = response.bodyAsText()
                AppLogger.d("PerformanceRepository", "DEBUG: Тело ошибки: $bodyText")
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("PerformanceRepository", "DEBUG: Исключение при загрузке: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
}