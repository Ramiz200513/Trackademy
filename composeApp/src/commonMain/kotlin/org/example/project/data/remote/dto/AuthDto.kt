package org.example.project.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(
    val login: String,
    val password: String,
    val organizationId: String
)

@Serializable
data class MeDto(
    val id: String,
    val login: String,
    val fullName: String,
    val role: Int,
    val organizationId: String,
    val organizationName: String? = null,
    val hasAcceptedTerms: Boolean = false
)
@Serializable
data class RefreshRequest(
    val refreshToken: String
)
@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String? = null, // ВАЖНО!
    val user: UserInfoDto? = null     // ВАЖНО!
)
@Serializable
data class UserInfoDto(
    val id: String? = null,
    val login: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val organizationId: String? = null
)