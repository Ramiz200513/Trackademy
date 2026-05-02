package org.example.project.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.core.ui.theme.AppTheme

@Composable
fun LoadingScreen() {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier.fillMaxSize().background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = colors.accent)
    }
}