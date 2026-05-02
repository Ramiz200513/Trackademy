package org.example.project.data.repository

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.DashboardDto
import org.example.project.domain.repository.DashboardRepository

class DashboardRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : DashboardRepository {

    override suspend fun getTeacherDashboard(): Result<DashboardDto, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val response = httpClient.get("api/Dashboard/teacher") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.value in 200..299) {
                Result.Success(response.body<DashboardDto>())
            } else {
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.Unknown)
        }
    }
}