package org.example.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val login: String,
    val fullName: String,
    val organizationName: String?,
    val role: Int
)

@Serializable
data class OrganizationDto(
    val id: String,
    val name: String
)
