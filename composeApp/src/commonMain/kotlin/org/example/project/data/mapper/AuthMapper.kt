package org.example.project.data.mapper

import org.example.project.data.remote.dto.OrganizationDto
import org.example.project.data.remote.dto.UserDto
import org.example.project.domain.models.Organization
import org.example.project.domain.models.User

fun UserDto.toDomain(): User = User(
    id = id,
    login = login,
    fullName = fullName,
    organizationName = organizationName ?: "Без организации",
    role = role
)

fun OrganizationDto.toDomain(): Organization = Organization(
    id = id,
    name = name
)