package org.example.project.feature.component
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.ui.theme.*
@Composable
fun DashboardHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Привет, $userName! 👋",
                color = TextWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Твой прогресс за неделю",
                color = TextGray,
                fontSize = 14.sp
            )
        }
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = AccentBlue.copy(alpha = 0.2f),
            border = BorderStroke(2.dp, AccentBlue)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(userName.take(1), color = AccentBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}