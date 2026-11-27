package org.example.project.feature.auth

import com.arkivanov.decompose.ComponentContext
import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.core.presentation.BaseComponent
import org.example.project.core.session.SessionStorage
import org.example.project.core.ui.error.toUserMessage
import org.example.project.domain.repository.AuthRepository
import org.example.project.domain.repository.RegisterParams

class AuthComponent(
    componentContext: ComponentContext,
    private val repository: AuthRepository,
    private val session: SessionStorage,
    private val onRootNavigate: () -> Unit
) : BaseComponent<AuthState, Nothing>(componentContext, AuthState()) {

    init {
        handleIntent(AuthIntent.LoadOrganizations)
    }

    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.LoadOrganizations -> loadOrganizations()
            is AuthIntent.UpdateRegistrationField -> updateState { copy(registrationDraft = intent.draft) }
            is AuthIntent.RegisterClicked -> register()
            is AuthIntent.ToggleAuthMode -> updateState {
                val newMode = if (authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                copy(authMode = newMode, error = null)
            }
            is AuthIntent.LoginClicked -> login()
            is AuthIntent.UpdateLoginFields -> updateState {
                copy(loginInput = intent.login, passwordInput = intent.pass, selectedOrgId = intent.orgId)
            }
        }
    }

    private fun login() {
        if (currentState.isLoading) return
        launch {
            updateState { copy(isLoading = true, error = null) }
            val result = repository.login(
                login = currentState.loginInput.trim(),
                pass = currentState.passwordInput,
                orgId = currentState.selectedOrgId
            )
            when (result) {
                is Result.Success -> onRootNavigate()
                is Result.Error -> {
                    val message = when (result.error) {
                        NetworkError.Unauthorized, NetworkError.BadRequest -> "Неверный логин или пароль"
                        else -> result.error.toUserMessage()
                    }
                    updateState { copy(isLoading = false, error = message) }
                }
            }
        }
    }

    private fun loadOrganizations() {
        launch {
            when (val result = repository.getOrganizations()) {
                is Result.Success -> updateState { copy(organizations = result.data) }
                is Result.Error -> updateState { copy(error = result.error.toUserMessage()) }
            }
        }
    }

    private fun register() {
        if (currentState.isLoading) return
        launch {
            updateState { copy(isLoading = true, error = null) }
            val draft = currentState.registrationDraft
            val params = RegisterParams(
                fullName = draft.fullName,
                login = draft.login,
                password = draft.password,
                phone = draft.phone,
                birthday = draft.birthday,
                organizationId = draft.organizationId,
                activationKey = draft.activationKey
            )
            when (val result = repository.register(params)) {
                is Result.Success -> {
                    session.organizationId = draft.organizationId
                    session.fullName = draft.fullName
                    onRootNavigate()
                }
                is Result.Error -> updateState { copy(isLoading = false, error = result.error.toUserMessage()) }
            }
        }
    }
}
