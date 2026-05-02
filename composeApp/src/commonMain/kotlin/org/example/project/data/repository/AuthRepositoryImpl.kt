package org.example.project.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import com.russhwolf.settings.Settings
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import org.example.project.core.network.Result
import org.example.project.core.domain.NetworkError
import org.example.project.data.remote.dto.*
import org.example.project.data.mapper.toDomain
import org.example.project.domain.models.*
import org.example.project.domain.repository.AuthRepository
import org.example.project.core.network.Result.Success
import org.example.project.core.network.Result.Error
import org.example.project.domain.repository.RegisterParams

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : AuthRepository {

    override suspend fun getOrganizations(): Result<List<Organization>, NetworkError> {
        return safeApiCall {
            val dtos = httpClient.get("api/Organization").body<List<OrganizationDto>>()
            dtos.map { it.toDomain() }
        }
    }

    override suspend fun login(login: String, pass: String, orgId: String): Result<Unit, NetworkError> {
        return try {
            val response = httpClient.post("api/Auth/login") {
                setBody(LoginRequest(login, pass, orgId))
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.OK) {
                val data = response.body<LoginResponse>()
                settings.putString("token", data.token)
                if (data.refreshToken != null) {
                    settings.putString("refreshToken", data.refreshToken)
                }
                settings.putString("orgId", orgId)
                data.user?.fullName?.let { settings.putString("fullName", it) }

                val roleString = data.user?.role
                val userRole = when (roleString) {
                    "Student" -> 1
                    "Administrator" -> 2
                    "Teacher" -> 3
                    else -> 0
                }
                settings.putInt("userRole", userRole)

                Result.Success(Unit)
            } else {
                Result.Error(NetworkError.Unknown)
            }
        } catch (e: Exception) {
            println("LOGIN EXCEPTION: ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }
    override suspend fun register(params:RegisterParams):Result<Unit, NetworkError>{
        return safeApiCall {

            httpClient.post("api/auth/register-admin") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(
                    fullName = params.fullName,
                    login = params.login,
                    password = params.password,
                    phone = params.phone,
                    birthday = params.birthday,
                    organizationId = params.organizationId,
                    activationKey = params.activationKey
                ))
            }
            Unit
        }
    }

    override suspend fun getCurrentUser(): Result<User, NetworkError> {
            return safeApiCall {
                val token = settings.getString("token","")
                val response = httpClient.get("api/Auth/me"){
                    header(HttpHeaders.Authorization,"Bearer $token")
                }
                response.body<UserDto>().toDomain()
            }
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): Result<T, NetworkError> {
        return try {
            Result.Success(block())
        } catch (e: io.ktor.client.plugins.ClientRequestException) {
            Result.Error(NetworkError.Unknown)
        } catch (e: io.ktor.utils.io.errors.IOException) {
            Result.Error(NetworkError.NoInternet)
        } catch (e: Exception) {
            Result.Error(NetworkError.ServerError)
        }
    }
}