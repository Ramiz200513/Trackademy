package org.example.project.domain.repository
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.DashboardDto

interface DashboardRepository {
    suspend fun getTeacherDashboard(): Result<DashboardDto, NetworkError>
}