package org.example.project.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

val LocalThemeIsDark = compositionLocalOf { true }

@Composable
fun AppTheme(
    isDark: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (isDark) DarkColors else LightColors

    CompositionLocalProvider(
        LocalAppColors provides colors,
        LocalThemeIsDark provides isDark,
        content = content
    )
}

// удобный accessor
object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
}