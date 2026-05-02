package org.example.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val fullName: String,
    val login: String,
    val password: String,
    val phone: String,
    val birthday: String,
    val organizationId: String,
    val activationKey: String
)