package org.example.project.core.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTypography {
    val h1 = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
    val h2 = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    val h3 = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    val h4 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

    val body1 = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal)
    val body2 = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal)
    val caption = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal)
    val label  = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium)
}