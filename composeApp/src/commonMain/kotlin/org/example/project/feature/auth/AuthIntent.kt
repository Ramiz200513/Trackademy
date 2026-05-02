package org.example.project.feature.auth

sealed interface AuthIntent {
    data object LoadOrganizations : AuthIntent
    data object RegisterClicked : AuthIntent
    data object ToggleAuthMode : AuthIntent
    data object LoginClicked : AuthIntent
    data class UpdateRegistrationField(val draft: RegistrationDraft) : AuthIntent
    data class UpdateLoginFields(val login: String, val pass: String, val orgId: String) : AuthIntent
}