package org.example.project.domain.repository

import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.CoinBalanceDto
import org.example.project.data.remote.dto.MarketItemDto
import org.example.project.data.remote.dto.PurchaseHistoryDto
import org.example.project.data.remote.dto.UserProfileDto

interface ProfileRepository {
    suspend fun getMe(): Result<UserProfileDto, NetworkError>
    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<String, NetworkError>
    suspend fun getMarketItems(organizationId: String): Result<List<MarketItemDto>, NetworkError>
    suspend fun getCoinBalance(studentId: String, organizationId: String): Result<CoinBalanceDto, NetworkError>
    suspend fun purchaseItem(organizationId: String, itemId: String, studentId: String): Result<String, NetworkError>
    suspend fun getPurchaseHistory(studentId: String, organizationId: String): Result<List<PurchaseHistoryDto>, NetworkError>
}