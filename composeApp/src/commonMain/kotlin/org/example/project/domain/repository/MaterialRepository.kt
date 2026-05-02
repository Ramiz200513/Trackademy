package org.example.project.domain.repository

import org.example.project.core.network.Result
import org.example.project.core.domain.NetworkError
import org.example.project.domain.models.Material

interface MaterialRepository {
    suspend fun getMaterials(
        organizationId: String,
        groupId: String? = null,
        searchTitle: String? = null,
        pageNumber: Int = 1,
        pageSize: Int = 20
    ): Result<List<Material>, NetworkError>

    suspend fun downloadMaterial(materialId: String): Result<ByteArray, NetworkError>
    suspend fun previewMaterial(materialId: String): Result<ByteArray, NetworkError>
}

