package org.example.project.core.logging

import org.example.project.core.config.AppConfig

/**
 * Minimal logger used instead of println.
 * Debug/info output is emitted only in debug builds, so release builds never
 * leak request bodies or tokens into device logs.
 */
object AppLogger {

    var isEnabled: Boolean = AppConfig.isDebug

    fun d(tag: String, message: String) {
        if (isEnabled) println("D/$tag: $message")
    }

    fun i(tag: String, message: String) {
        if (isEnabled) println("I/$tag: $message")
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (!isEnabled) return
        println("E/$tag: $message")
        throwable?.let { println("E/$tag: ${it::class.simpleName}: ${it.message}") }
    }
}
