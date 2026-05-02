package org.example.project.domain.repository

import org.example.project.core.domain.NetworkError
import org.example.project.domain.models.Organization
import org.example.project.domain.models.User
import org.example.project.core.network.Result

interface AuthRepository {
    suspend fun getOrganizations(): Result<List<Organization>, NetworkError>

    suspend fun login(login: String, pass: String, orgId: String): Result<Unit, NetworkError>

    suspend fun register(params: RegisterParams): Result<Unit, NetworkError>

    suspend fun getCurrentUser(): Result<User, NetworkError>
}

data class LoginParams(
    val login: String,
    val password: String,
    val organizationId: String
)

data class RegisterParams(
    val fullName: String,
    val login: String,
    val password: String,
    val phone: String,
    val birthday: String,
    val organizationId: String,
    val activationKey: String
)