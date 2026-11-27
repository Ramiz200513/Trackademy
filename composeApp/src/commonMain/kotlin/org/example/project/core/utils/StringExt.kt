package org.example.project.core.utils

/** "Иванов Иван Петрович" -> "ИИ". Returns [fallback] for blank input. */
fun String.toInitials(maxLetters: Int = 2, fallback: String = "??"): String =
    split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(maxLetters)
        .joinToString("")
        .ifEmpty { fallback }
