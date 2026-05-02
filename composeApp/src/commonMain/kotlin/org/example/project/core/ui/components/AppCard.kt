package org.example.project.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.core.ui.theme.AppTheme
import org.example.project.core.ui.theme.Radius
import org.example.project.core.ui.theme.Spacing

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accentColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = AppTheme.colors
    val borderColor = accentColor?.copy(alpha = 0.3f) ?: colors.cardBorder

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.lg))
            .background(colors.card)
            .border(1.dp, borderColor, RoundedCornerShape(Radius.lg))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(Spacing.lg),
        content = content
    )
}