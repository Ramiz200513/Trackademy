package org.example.project.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.core.network.ensureSuccess
import org.example.project.core.network.safeApiCall
import org.example.project.core.session.SessionStorage
import org.example.project.core.session.UserRole
import org.example.project.data.mapper.toDomain
import org.example.project.data.remote.dto.LoginRequest
import org.example.project.data.remote.dto.LoginResponse
import org.example.project.data.remote.dto.OrganizationDto
import org.example.project.data.remote.dto.RegisterRequest
import org.example.project.data.remote.dto.UserDto
import org.example.project.domain.models.Organization
import org.example.project.domain.models.User
import org.example.project.domain.repository.AuthRepository
import org.example.project.domain.repository.RegisterParams

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val session: SessionStorage
) : AuthRepository {

    override suspend fun getOrganizations(): Result<List<Organization>, NetworkError> =
        safeApiCall(TAG) {
            httpClient.get("api/Organization")
                .ensureSuccess()
                .body<List<OrganizationDto>>()
                .map { it.toDomain() }
        }

    override suspend fun login(login: String, pass: String, orgId: String): Result<Unit, NetworkError> =
        safeApiCall(TAG) {
            val data = httpClient.post("api/Auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(login, pass, orgId))
            }.ensureSuccess().body<LoginResponse>()

            session.saveTokens(data.token, data.refreshToken)
            session.organizationId = orgId
            data.user?.fullName?.let { session.fullName = it }
            session.userRole = UserRole.fromApi(data.user?.role)
        }

    override suspend fun register(params: RegisterParams): Result<Unit, NetworkError> =
        safeApiCall(TAG) {
            httpClient.post("api/auth/register-admin") {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        fullName = params.fullName,
                        login = params.login,
                        password = params.password,
                        phone = params.phone,
                        birthday = params.birthday,
                        organizationId = params.organizationId,
                        activationKey = params.activationKey
                    )
                )
            }.ensureSuccess()
            Unit
        }

    override suspend fun getCurrentUser(): Result<User, NetworkError> =
        safeApiCall(TAG) {
            httpClient.get("api/Auth/me").ensureSuccess().body<UserDto>().toDomain()
        }

    private companion object {
        const val TAG = "AuthRepository"
    }
}
