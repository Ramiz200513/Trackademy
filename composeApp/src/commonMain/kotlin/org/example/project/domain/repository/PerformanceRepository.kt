package org.example.project.domain.repository
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.domain.models.Performance


interface PerformanceRepository {
    suspend fun getPerformanceData(): Result<Performance, NetworkError>
}