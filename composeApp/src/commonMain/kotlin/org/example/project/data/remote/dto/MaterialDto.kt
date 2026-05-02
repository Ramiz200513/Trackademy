package org.example.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MaterialDto(
    val id:String,
    val title: String,
    val description: String? = null,
    val originalFileName: String,
    val contentType: String,
    val fileSize: Long,
    val isPrivate: Boolean = false,
    val groupId: String? = null,
    val groupName: String? = null,
    val uploadedById: String? = null,
    val uploadedByName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class MaterialsResponse(
    val items: List<MaterialDto> = emptyList(),
    val totalCount: Int = 0,
    val pageNumber: Int = 1,
    val pageSize: Int = 10,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
)

