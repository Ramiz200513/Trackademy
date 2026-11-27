package org.example.project.core.config

import kotlin.experimental.ExperimentalNativeApi

actual object AppConfig {
    actual val apiBaseUrl: String = "https://dev.trackademy.kz/"

    @OptIn(ExperimentalNativeApi::class)
    actual val isDebug: Boolean = Platform.isDebugBinary
}
