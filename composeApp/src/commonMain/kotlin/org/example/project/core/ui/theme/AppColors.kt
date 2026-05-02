package org.example.project.core.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val cardBorder: Color,
    val inputBackground: Color,

    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,

    val accent: Color,
    val accentVariant: Color,

    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,

    val isDark: Boolean
)

val DarkColors = AppColors(
    background     = Dark50,
    surface        = Dark100,
    card           = Dark200,
    cardBorder     = Dark400,
    inputBackground = Dark300,

    textPrimary    = TextPrimary,
    textSecondary  = TextSecondary,
    textDisabled   = TextDisabled,

    accent         = Purple500,
    accentVariant  = Purple700,

    success        = Success,
    warning        = Warning,
    error          = Error,
    info           = Info,

    isDark         = true
)

val LightColors = AppColors(
    background     = Light100,
    surface        = Light50,
    card           = Light200,
    cardBorder     = Light300,
    inputBackground = Light200,

    textPrimary    = TextDark,
    textSecondary  = Color(0xFF475569),
    textDisabled   = Color(0xFF94A3B8),

    accent         = Purple500,
    accentVariant  = Purple700,

    success        = Success,
    warning        = Warning,
    error          = Error,
    info           = Info,

    isDark         = false
)

val LocalAppColors = staticCompositionLocalOf { DarkColors }