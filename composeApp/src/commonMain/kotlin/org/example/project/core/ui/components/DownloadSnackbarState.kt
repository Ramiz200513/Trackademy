package org.example.project.core.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.ui.theme.*
sealed class DownloadSnackbarState {
    object Hidden : DownloadSnackbarState()
    object InProgress : DownloadSnackbarState()
    data class Success(val fileName: String) : DownloadSnackbarState()
    data class Error(val message: String) : DownloadSnackbarState()
}

@Composable
fun DownloadSnackbar(
    state: DownloadSnackbarState,
    onOpen: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Авто-скрытие через 4 секунды после успеха/ошибки
    LaunchedEffect(state) {
        if (state is DownloadSnackbarState.Success || state is DownloadSnackbarState.Error) {
            kotlinx.coroutines.delay(4_000)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = state !is DownloadSnackbarState.Hidden,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        val (borderColor, icon, text) = when (state) {
            is DownloadSnackbarState.InProgress -> Triple(Blue.copy(alpha = 0.5f), null, "Скачивание...")
            is DownloadSnackbarState.Success    -> Triple(Green.copy(alpha = 0.5f), "✅", state.fileName)
            is DownloadSnackbarState.Error      -> Triple(Red.copy(alpha = 0.5f), "❌", state.message)
            else -> Triple(Color.Transparent, null, "")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E1E2E))
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Иконка / спиннер
            if (state is DownloadSnackbarState.InProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Blue,
                    strokeWidth = 2.dp
                )
            } else if (icon != null) {
                Text(icon, fontSize = 16.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                val label = when (state) {
                    is DownloadSnackbarState.InProgress -> "Скачивание файла"
                    is DownloadSnackbarState.Success    -> "Файл сохранён"
                    is DownloadSnackbarState.Error      -> "Ошибка"
                    else -> ""
                }
                Text(label, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    text,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            if (state is DownloadSnackbarState.Success) {
                TextButton(onClick = onOpen) {
                    Text(
                        "Открыть",
                        color = Blue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state is DownloadSnackbarState.Error) {
                TextButton(onClick = onDismiss) {
                    Text("✕", color = TextMuted, fontSize = 13.sp)
                }
            }
        }
    }
}