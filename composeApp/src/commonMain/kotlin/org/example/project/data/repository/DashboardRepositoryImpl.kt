package org.example.project.data.repository

import org.example.project.core.logging.AppLogger
import org.example.project.core.network.toNetworkError

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
            val response = httpClient.get("api/Dashboard/teacher")
            if (response.status.value in 200..299) {
                Result.Success(response.body<DashboardDto>())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }
}