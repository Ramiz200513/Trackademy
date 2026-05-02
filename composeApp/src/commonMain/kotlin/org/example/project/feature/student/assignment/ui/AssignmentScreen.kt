
package org.example.project.feature.student.assignment.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.models.Assignment
import org.example.project.domain.models.AssignmentGroup
import org.example.project.feature.student.assignment.AssignmentComponent
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import org.example.project.data.remote.dto.AssignmentDetailDto
import org.example.project.core.ui.theme.*

private val statusColors = mapOf(
    "Просрочено" to Color(0xFFEF4444),
    "К выполнению" to Color(0xFF6C3FCF),
    "На проверке" to Color(0xFF3B82F6),
    "Проверено" to Color(0xFF10B981)
)

@Composable
fun AssignmentScreen(component: AssignmentComponent) {
    val state by component.state.collectAsState()
    var textWork by remember { mutableStateOf("") }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    val scope = rememberCoroutineScope()
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                selectedImageBytes = it
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF3D1F8C), PurpleAccent)))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        "Домашние задания",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${state.groups.sumOf { it.assignments.size }} заданий",
                        color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurpleAccent)
                }
            } else if (state.error != null) {
                // Если произошла ошибка сети
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📡", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            state.error ?: "Ошибка",
                            color = Color(0xFFEF4444),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (state.groups.isEmpty()) {
                // Если бэкенд вернул пустоту
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎉", fontSize = 40.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Нет активных заданий",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text("Можно немного отдохнуть", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.groups.forEach { group ->
                        item {
                            GroupHeader(group)
                        }
                        items(group.assignments) { assignment ->
                            AssignmentCard(
                                assignment = assignment,
                                onClick = { component.selectAssignment(assignment) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = state.selectedAssignment != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            state.selectedAssignment?.let { assignment ->
                AssignmentDetailSheet(
                    assignment = assignment,
                    details = state.selectedDetails,
                    onClose = { component.closeDetail() },
                    onSubmit = { textContent, imageBytes ->
                        component.submitWork(assignment.id, textContent, imageBytes)
                    },
                    onDownloadFile = { url, fileName ->
                        component.downloadAndSaveFile(url, fileName)
                    }
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(group: AssignmentGroup) {
    val color = statusColors[group.status] ?: TextMuted
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(
            "${group.status} (${group.assignments.size})",
            color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun AssignmentCard(assignment: Assignment, onClick: () -> Unit) {
    val color = statusColors[assignment.status] ?: Color(0xFF6C3FCF)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(assignment.status, color = color, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(assignment.description, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(assignment.subjectName, color = TextMuted, fontSize = 12.sp)
                }
                if (assignment.score != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${assignment.score}", color = Color(0xFF10B981), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📅 Выдано: ${assignment.assignedDate}", color = TextDim, fontSize = 11.sp)
                Text("⏰ Срок: ${assignment.dueDate}", color = if (assignment.isOverdue) Color(0xFFEF4444) else TextDim, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AssignmentDetailSheet(
    assignment: Assignment,
    details: AssignmentDetailDto?,
    onClose: () -> Unit,
    onSubmit: (String, ByteArray?) -> Unit,
    onDownloadFile: (String, String) -> Unit
) {
    var textWork by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current

    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    val scope = rememberCoroutineScope()
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                selectedImageBytes = it
            }
        }
    )

    val sheetBg = Color(0xFF2B2D3C)
    val inputBg = Color(0xFF333544)
    val purplePrimary = Color(0xFF8B5CF6)
    val textLight = Color(0xFFF3F4F6)
    val textDimmed = Color(0xFF9CA3AF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(sheetBg)
                .clickable(enabled = false) {}
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(purplePrimary)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = assignment.description,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Закрыть", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Text("Детали задания", color = textLight, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    DetailGridItem("Описание", assignment.description, textLight)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth()) {
                        DetailGridItem("Предмет", assignment.subjectName, textLight, Modifier.weight(1f))
                        DetailGridItem("Группа", assignment.groupName, textLight, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    DetailGridItem("Статус", assignment.status, textLight)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth()) {
                        DetailGridItem("Выдано", assignment.assignedDate, textLight, Modifier.weight(1f))
                        DetailGridItem("Срок сдачи", assignment.dueDate, textLight, Modifier.weight(1f))
                    }
                }

                item { HorizontalDivider(color = Color(0xFF3F4152)) }

                if (details == null) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = purplePrimary, modifier = Modifier.size(24.dp))
                        }
                    }
                } else {
                    if (details.hasAttachment && details.attachmentName != null) {
                        item {
                            Text("Файл от преподавателя", color = textLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f)) // Синий фон
                                    .clickable {
                                        if (details != null) {
                                            onDownloadFile("Assignment/${details.id}/download", details.attachmentName ?: "file")                              }
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("📎", fontSize = 20.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(details.attachmentName, color = Color(0xFF3B82F6), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text("${(details.attachmentSize ?: 0) / 1024} KB", color = textDimmed, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    val studentSubmission = details.studentSubmissions.firstOrNull()?.submission
                    if (studentSubmission != null && studentSubmission.files.isNotEmpty()) {
                        item {
                            Text("Ваши загруженные работы", color = textLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))

                            // Перебираем все загруженные файлы
                            studentSubmission.files.forEach { file ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(inputBg) // Серый фон
                                        .clickable {
                                            onDownloadFile("Submission/file/${file.id}", file.originalFileName)
                                        }
                                        .padding(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("📄", fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(file.originalFileName, color = textLight, fontSize = 14.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                // --- КОНЕЦ БЛОКА ФАЙЛОВ ---

                item { HorizontalDivider(color = Color(0xFF3F4152)) }

                // БЛОК 2: Текст работы
                item {
                    Text("Комментарий / Текст работы", color = textLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = textWork,
                        onValueChange = { textWork = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        placeholder = { Text("Введите текст вашей работы...", color = textDimmed, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = inputBg,
                            unfocusedContainerColor = inputBg,
                            focusedBorderColor = purplePrimary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = textLight,
                            unfocusedTextColor = textLight
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // БЛОК 3: Прикрепление файлов
                item {
                    Text("Прикрепить файлы", color = textLight, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))

                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .drawBehind {
                                drawRoundRect(
                                    color = Color(0xFF6B7280),
                                    style = Stroke(width = 3f, pathEffect = dashEffect),
                                    cornerRadius = CornerRadius(12.dp.toPx())
                                )
                            }
                            .clickable {
                                singleImagePicker.launch()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedImageBytes != null) {
                                Text("✅ Фото выбрано!", color = Color(0xFF10B981), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("Размер: ${selectedImageBytes!!.size / 1024} KB", color = textDimmed, fontSize = 12.sp)
                            } else {
                                Text("↑", color = textDimmed, fontSize = 24.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Нажмите для выбора из галереи", color = textDimmed, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ФУТЕР С КНОПКАМИ
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(sheetBg)
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onClose) {
                    Text("Отмена", color = textLight, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        onSubmit(textWork, selectedImageBytes)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = purplePrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Сдать работу", color = Color.White)
                }
            }
        }
    }
}
@Composable
private fun DetailGridItem(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, color = Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun DetailRow(label: String, value: String, valueColor: Color = TextWhite) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
}