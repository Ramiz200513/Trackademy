package org.example.project.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileDto(
    val id: String,
    val login: String,
    val fullName: String,
    val role: Int,
    val organizationId: String? = null,
    val organizationName: String? = null,
    val hasAcceptedTerms: Boolean = false
) {
    val roleName: String get() = when (role) {
        1 -> "Студент"
        2 -> "Учитель"
        3 -> "Администратор"
        else -> "Пользователь"
    }
}

@Serializable
data class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

@Serializable
data class UpdatePasswordResponse(
    val message: String? = null
)

@Serializable
data class MarketItemDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Int,
    val imageUrl: String? = null,
    val isAvailable: Boolean = true,
    val itemType: Int = 1,
    val itemTypeName: String? = null,
    val stockQuantity: Int = 0,
    val maxPerStudent: Int = 1,
    val isActive: Boolean = true
)

@Serializable
data class CoinBalanceDto(
    val studentId: String? = null,
    val organizationId: String? = null,
    val balance: Int = 0
)

@Serializable
data class PurchaseRequest(
    @SerialName("MarketItemId")
    val itemId: String,
    val studentId: String,
    val organizationId: String
)
@Serializable
data class PurchaseHistoryDto(
    val id: String,
    val itemName: String,
    val pricePaid: Int,
    val status: Int = 0,
    val statusName: String? = null,
    val createdAt: String? = null,
    val processedAt: String? = null
)