package org.example.project.domain.models

data class User(
    val id: String,
    val login: String,
    val fullName: String,
    val organizationName: String,
    val role: Int
)