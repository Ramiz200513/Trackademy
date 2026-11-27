package org.example.project.data.repository

import org.example.project.core.logging.AppLogger
import org.example.project.core.network.toNetworkError

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.CoinBalanceDto
import org.example.project.data.remote.dto.MarketItemDto
import org.example.project.data.remote.dto.PurchaseHistoryDto
import org.example.project.data.remote.dto.PurchaseRequest
import org.example.project.data.remote.dto.UpdatePasswordRequest
import org.example.project.data.remote.dto.UpdatePasswordResponse
import org.example.project.data.remote.dto.UserProfileDto
import org.example.project.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : ProfileRepository {

    override suspend fun getMe(): Result<UserProfileDto, NetworkError> {
        return try {
            val response = httpClient.get("api/Auth/me")
            if (response.status.value in 200..299) {
                Result.Success(response.body<UserProfileDto>())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("ProfileRepository", "PROFILE ERROR: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
    override suspend fun purchaseItem(
        organizationId: String,
        itemId: String,
        studentId: String
    ): Result<String, NetworkError> {
        return try {
            val body = Json.encodeToString(PurchaseRequest(itemId, studentId, organizationId))
            val response = httpClient.post("api/Purchase/organization/$organizationId") {
                setBody(TextContent(body, ContentType.Application.Json))
            }
            if (response.status.value in 200..299) {
                Result.Success("Покупка успешна!")
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun getPurchaseHistory(
        studentId: String,
        organizationId: String
    ): Result<List<PurchaseHistoryDto>, NetworkError> {
        return try {
            val response = httpClient.get("api/Purchase/student/$studentId/organization/$organizationId")
            if (response.status.value in 200..299) {
                Result.Success(response.body<List<PurchaseHistoryDto>>())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }
    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Result<String, NetworkError> {
        return try {
            val body = Json.encodeToString(UpdatePasswordRequest(currentPassword, newPassword))
            val response = httpClient.put("api/User/update-password") {
                setBody(TextContent(body, ContentType.Application.Json))
            }
            if (response.status.value in 200..299) {
                val resp = response.body<UpdatePasswordResponse>()
                Result.Success(resp.message ?: "Пароль успешно обновлён")
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("ProfileRepository", "PASSWORD ERROR: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun getMarketItems(organizationId: String): Result<List<MarketItemDto>, NetworkError> {
        return try {
            val response = httpClient.get("api/MarketItem/organization/$organizationId")
            if (response.status.value in 200..299) {
                Result.Success(response.body<List<MarketItemDto>>())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("ProfileRepository", "MARKET ERROR: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun getCoinBalance(
        studentId: String,
        organizationId: String
    ): Result<CoinBalanceDto, NetworkError> {
        return try {
            val response = httpClient.get("api/Coin/$studentId/organization/$organizationId")
            if (response.status.value in 200..299) {
                Result.Success(response.body<CoinBalanceDto>())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("ProfileRepository", "COIN ERROR: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
}