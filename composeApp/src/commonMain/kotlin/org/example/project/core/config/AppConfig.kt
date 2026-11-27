package org.example.project.core.config

/**
 * Build-time configuration provided by each platform.
 */
expect object AppConfig {
    /** Base URL of the Trackademy REST API, always ends with "/". */
    val apiBaseUrl: String

    /** True for debug builds: enables verbose logging. */
    val isDebug: Boolean
}
