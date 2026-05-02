package org.example.project.feature.profile

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.CoinBalanceDto
import org.example.project.data.remote.dto.MarketItemDto
import org.example.project.data.remote.dto.PurchaseHistoryDto
import org.example.project.data.remote.dto.UserProfileDto
import org.example.project.domain.repository.ProfileRepository

class ProfileComponent(
    componentContext: ComponentContext,
    private val repository: ProfileRepository,
    private val settings: Settings,
    val onLogout: () -> Unit
) : ComponentContext by componentContext {
    fun setMarketTab(tab: MarketTab) {
        _state.update { it.copy(marketTab = tab) }
        if (tab == MarketTab.HISTORY) loadPurchaseHistory()
    }

    private fun loadPurchaseHistory() {
        val profile = _state.value.profile ?: return
        val orgId = profile.organizationId ?: settings.getString("orgId", "")
        scope.launch {
            when (val result = repository.getPurchaseHistory(profile.id, orgId)) {
                is Result.Success -> _state.update { it.copy(purchaseHistory = result.data) }
                is Result.Error -> {}
            }
        }
    }

    fun purchaseItem(itemId: String) {
        val profile = _state.value.profile ?: return
        val orgId = profile.organizationId ?: settings.getString("orgId", "")
        scope.launch {
            _state.update { it.copy(isPurchasing = true, purchaseError = null) }
            when (val result = repository.purchaseItem(orgId, itemId, profile.id)) {
                is Result.Success -> {
                    _state.update { it.copy(isPurchasing = false, purchaseSuccess = result.data) }
                    // Обновить баланс
                    loadCoinBalance(profile.id, orgId)
                }
                is Result.Error -> {
                    _state.update { it.copy(isPurchasing = false, purchaseError = "Не удалось купить товар") }
                }
            }
        }
    }

    fun clearPurchaseMessage() {
        _state.update { it.copy(purchaseSuccess = null, purchaseError = null) }
    }
    private val scope = coroutineScope()

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getMe()) {
                is Result.Success -> {
                    val profile = result.data
                    _state.update { it.copy(profile = profile, isLoading = false) }
                    // Load coin balance after getting profile
                    val orgId = profile.organizationId ?: settings.getString("orgId", "")
                    if (orgId.isNotBlank()) {
                        loadCoinBalance(profile.id, orgId)
                    }
                }
                is Result.Error -> _state.update { it.copy(isLoading = false, error = "Не удалось загрузить профиль") }
            }
        }
    }

    private fun loadCoinBalance(studentId: String, organizationId: String) {
        scope.launch {
            when (val result = repository.getCoinBalance(studentId, organizationId)) {
                is Result.Success -> _state.update { it.copy(coinBalance = result.data) }
                is Result.Error -> { /* баланс может быть недоступен — не ошибка */ }
            }
        }
    }

    fun openMarket() {
        val orgId = _state.value.profile?.organizationId ?: settings.getString("orgId", "")
        _state.update { it.copy(showMarket = true, marketItems = emptyList(), isMarketLoading = true) }
        scope.launch {
            when (val result = repository.getMarketItems(orgId)) {
                is Result.Success -> _state.update { it.copy(marketItems = result.data, isMarketLoading = false) }
                is Result.Error -> _state.update { it.copy(isMarketLoading = false, marketError = "Не удалось загрузить товары") }
            }
        }
    }

    fun closeMarket() {
        _state.update { it.copy(showMarket = false, marketError = null) }
    }

    fun openChangePassword() {
        _state.update { it.copy(showPasswordDialog = true, passwordError = null, passwordSuccess = null) }
    }

    fun closeChangePassword() {
        _state.update { it.copy(showPasswordDialog = false, passwordError = null, passwordSuccess = null) }
    }

    fun changePassword(current: String, newPass: String, confirm: String) {
        if (newPass != confirm) {
            _state.update { it.copy(passwordError = "Пароли не совпадают") }
            return
        }
        if (newPass.length < 6) {
            _state.update { it.copy(passwordError = "Пароль должен быть не менее 6 символов") }
            return
        }
        scope.launch {
            _state.update { it.copy(isPasswordLoading = true, passwordError = null) }
            when (val result = repository.updatePassword(current, newPass)) {
                is Result.Success -> {
                    _state.update { it.copy(isPasswordLoading = false, passwordSuccess = result.data, showPasswordDialog = false) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isPasswordLoading = false, passwordError = "Неверный текущий пароль") }
                }
            }
        }
    }

    fun logout() {
        onLogout()
    }

    fun clearMessage() {
        _state.update { it.copy(passwordSuccess = null, passwordError = null) }
    }
}

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: UserProfileDto? = null,
    val coinBalance: CoinBalanceDto? = null,
    val error: String? = null,
    // Market
    val showMarket: Boolean = false,
    val marketItems: List<MarketItemDto> = emptyList(),
    val isMarketLoading: Boolean = false,
    val marketError: String? = null,
    val purchaseHistory: List<PurchaseHistoryDto> = emptyList(),
    val isPurchasing: Boolean = false,
    val purchaseSuccess: String? = null,
    val purchaseError: String? = null,
    val marketTab: MarketTab = MarketTab.SHOP,
    // Password
    val showPasswordDialog: Boolean = false,
    val isPasswordLoading: Boolean = false,
    val passwordError: String? = null,
    val passwordSuccess: String? = null
)

enum class MarketTab { SHOP, HISTORY }