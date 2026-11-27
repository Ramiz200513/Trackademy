package org.example.project.data.repository

import org.example.project.core.logging.AppLogger
import org.example.project.core.network.toNetworkError

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpHeaders
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.MaterialsResponse
import org.example.project.domain.models.Material
import org.example.project.domain.repository.MaterialRepository

class MaterialRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : MaterialRepository {

    override suspend fun getMaterials(
        organizationId: String,
        groupId: String?,
        searchTitle: String?,
        pageNumber: Int,
        pageSize: Int
    ): Result<List<Material>, NetworkError> {
        return try {

            val params = buildString {
                append("OrganizationId=$organizationId")
                append("&PageNumber=$pageNumber")
                append("&PageSize=$pageSize")
                if (!groupId.isNullOrBlank()) append("&GroupId=$groupId")
                if (!searchTitle.isNullOrBlank()) append("&SearchTitle=$searchTitle")
            }

            val response = httpClient.get("api/Material?$params")

            if (response.status.value in 200..299) {
                val dto = response.body<MaterialsResponse>()
                Result.Success(dto.items.map { it.toDomain() })
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("MaterialRepository", "MATERIAL ERROR: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun downloadMaterial(materialId: String): Result<ByteArray, NetworkError> {
        return try {
            val response = httpClient.get("api/Material/$materialId/download")
            if (response.status.value in 200..299) {
                Result.Success(response.readBytes())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun previewMaterial(materialId: String): Result<ByteArray, NetworkError> {
        return try {
            val response = httpClient.get("api/Material/$materialId/preview")
            if (response.status.value in 200..299) {
                Result.Success(response.readBytes())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }
}

private fun org.example.project.data.remote.dto.MaterialDto.toDomain() = Material(
    id = id,
    title = title,
    description = description,
    originalFileName = originalFileName,
    contentType = contentType,
    fileSize = fileSize,
    groupId = groupId,
    groupName = groupName,
    uploadedByName = uploadedByName,
    createdAt = createdAt?.take(10)
)