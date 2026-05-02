package org.example.project.core.di

import com.russhwolf.settings.Settings

interface TokenProvider {
    fun getToken(): String?
    fun getRefreshToken(): String?
}


class SettingsTokenProvider(private val settings: Settings) : TokenProvider {
    override fun getToken(): String? {
        val token = settings.getString("token", "")
        return if (token.isBlank()) null else token
    }

    override fun getRefreshToken(): String? {
        val refresh = settings.getString("refreshToken", "")
        return if (refresh.isBlank()) null else refresh
    }
}