package org.example.project.core.config

import org.example.project.BuildConfig

actual object AppConfig {
    actual val apiBaseUrl: String = BuildConfig.API_BASE_URL
    actual val isDebug: Boolean = BuildConfig.DEBUG
}
