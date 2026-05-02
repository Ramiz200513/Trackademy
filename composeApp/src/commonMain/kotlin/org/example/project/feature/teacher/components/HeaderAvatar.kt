package org.example.project.feature.teacher.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.ui.theme.*

@Composable
fun HeaderAvatar(
    initials: String,
    onLogout: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { showMenu = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .background(CardDark)
                .width(160.dp)
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        "🚪  Выйти",
                        color = Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    showMenu = false
                    onLogout()
                }
            )
        }
    }
}