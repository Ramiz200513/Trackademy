package org.example.project.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.repository.AuthRepository
import org.example.project.domain.repository.RegisterParams


import org.example.project.core.network.Result

class AuthComponent(
    componentContext: ComponentContext,
    private val repository: AuthRepository,
    private val onRootNavigate: () -> Unit
): ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    init {
        handleIntent(AuthIntent.LoadOrganizations)
    }


    fun handleIntent(intent: AuthIntent) {
        when (intent) {
            is AuthIntent.LoadOrganizations -> loadOrgs()
            is AuthIntent.UpdateRegistrationField -> {
                _state.update { it.copy(registrationDraft = intent.draft) }
            }
            is AuthIntent.RegisterClicked -> register()
            is AuthIntent.ToggleAuthMode -> {
                _state.update {
                    val newMode = if (it.authMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                    it.copy(authMode = newMode, error = null)
                }
            }
            is AuthIntent.LoginClicked -> login()
            is AuthIntent.UpdateLoginFields -> {
                _state.update { it.copy(
                    loginInput = intent.login,
                    passwordInput = intent.pass,
                    selectedOrgId = intent.orgId
                ) }
            }
        }
    }
    private fun login() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = repository.login(
                login = _state.value.loginInput,
                pass = _state.value.passwordInput,
                orgId = _state.value.selectedOrgId
            )

            when (result) {
                is Result.Success -> onRootNavigate()
                is Result.Error -> _state.update { it.copy(isLoading = false, error = "Ошибка входа") }
            }
        }
    }
    private fun loadOrgs(){
        scope.launch {
            when(val result = repository.getOrganizations()){
                is Result.Success -> _state.update { it.copy(organizations = result.data) }
                is Result.Error -> _state.update { it.copy(error = "Ошибка загрузки организаций") }
            }
        }
    }

    private fun register() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            val draft = _state.value.registrationDraft

            val params = RegisterParams(
                fullName = draft.fullName,
                login = draft.login,
                password = draft.password,
                phone = draft.phone,
                birthday = draft.birthday,
                organizationId = draft.organizationId,
                activationKey = draft.activationKey
            )

            when (repository.register(params)) {
                is Result.Success -> onRootNavigate()
                is Result.Error -> _state.update { it.copy(isLoading = false, error = "Ошибка регистрации") }
            }
        }
    }
}