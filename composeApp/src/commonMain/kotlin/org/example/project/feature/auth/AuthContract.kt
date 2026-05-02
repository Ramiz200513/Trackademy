package org.example.project.feature.auth

import org.example.project.domain.models.Organization

data class RegistrationDraft(
    val fullName: String = "",
    val login: String = "",
    val password: String = "",
    val phone: String = "",
    val birthday: String = "",
    val organizationId: String = "",
    val activationKey: String = ""
)
enum class AuthMode {
    LOGIN, REGISTER
}
data class AuthState(
    val authMode: AuthMode = AuthMode.LOGIN,
    val isLoading: Boolean = false,
    val error: String? = null,
    val organizations: List<Organization> = emptyList(),
    val loginInput: String = "",
    val passwordInput: String = "",
    val selectedOrgId: String = "",
    val registrationDraft: RegistrationDraft = RegistrationDraft()
)