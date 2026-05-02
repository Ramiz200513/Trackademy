package org.example.project.feature.teacher.assignment.ui
import org.example.project.core.ui.theme.*
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.remote.dto.AssignmentDto
import org.example.project.data.remote.dto.GroupDto
import org.example.project.data.remote.dto.StudentSubmissionDto
import org.example.project.feature.teacher.TeacherAssignmentComponent
import org.example.project.feature.teacher.components.HeaderAvatar


@Composable
fun TeacherAssignmentScreen(component: TeacherAssignmentComponent) {
    val state by component.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Домашние задания", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Управление заданиями для групп", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HeaderAvatar(initials = state.userInitials, onLogout = component.onLogout)
                        FloatingActionButton(
                            onClick = { component.openCreate() },
                            containerColor = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            // ── Group filter ──────────────────────────────────────────────────
            GroupFilterRow(
                groups = state.groups,
                selectedGroupId = state.selectedGroupId,
                onGroupSelected = { component.selectGroup(it) }
            )

            // ── Content ───────────────────────────────────────────────────────
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Blue)
                }
            } else if (state.assignments.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📝", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Заданий пока нет", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("Нажмите + чтобы создать задание", color = TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.assignments) { assignment ->
                        AssignmentCard(
                            assignment = assignment,
                            onClick = { component.openDetail(assignment) }
                        )
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
                    onClose = { component.closeDetail() },
                    onDelete = { component.deleteAssignment(assignment.id) }
                )
            }
        }

        // ── Create dialog ─────────────────────────────────────────────────────
        if (state.showCreateDialog) {
            CreateAssignmentDialog(
                groups = state.groups,
                isSaving = state.isSaving,
                onConfirm = { groupId, desc, due -> component.createAssignment(groupId, desc, due) },
                onDismiss = { component.closeCreate() }
            )
        }
    }
}

// ── Group filter ──────────────────────────────────────────────────────────────
@Composable
private fun GroupFilterRow(
    groups: List<GroupDto>,
    selectedGroupId: String?,
    onGroupSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardDark)
                    .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    groups.find { it.id == selectedGroupId }?.name ?: "Все группы",
                    color = TextWhite,
                    fontSize = 13.sp
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CardDark)
            ) {
                DropdownMenuItem(
                    text = { Text("Все группы", color = TextWhite, fontSize = 13.sp) },
                    onClick = { onGroupSelected(null); expanded = false }
                )
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name, color = TextWhite, fontSize = 13.sp) },
                        onClick = { onGroupSelected(group.id); expanded = false }
                    )
                }
            }
        }
    }
}

// ── Assignment card ───────────────────────────────────────────────────────────
@Composable
private fun AssignmentCard(assignment: AssignmentDto, onClick: () -> Unit) {
    val submitted = assignment.studentSubmissions.count { it.submission != null }
    val total = assignment.studentSubmissions.size
    val allSubmitted = total > 0 && submitted == total

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, if (allSubmitted) Green.copy(alpha = 0.4f) else Blue.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    assignment.subjectName ?: "—",
                    color = Blue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    assignment.description,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (assignment.hasAttachment) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Orange.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text("📎", fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Группа: ${assignment.group?.name ?: "—"}", color = TextMuted, fontSize = 11.sp)
                Text("До: ${assignment.dueDate.take(10)}", color = TextMuted, fontSize = 11.sp)
            }
            if (total > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("$submitted/$total", color = if (allSubmitted) Green else Orange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("сдали", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        if (total > 0) {
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF1A1A28))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (total > 0) submitted.toFloat() / total else 0f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (allSubmitted) Green else Orange)
                )
            }
        }
    }
}

// ── Detail sheet ──────────────────────────────────────────────────────────────
@Composable
private fun AssignmentDetailSheet(
    assignment: AssignmentDto,
    onClose: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceDark)
                .clickable(enabled = false) {}
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Детали задания", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(assignment.group?.name ?: "—", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp).background(Red.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                // Info
                item {
                    InfoCard {
                        DetailRow("📚 Предмет", assignment.subjectName ?: "—")
                        DetailRow("📝 Описание", assignment.description)
                        DetailRow("📅 Выдано", assignment.assignedDate.take(10))
                        DetailRow("⏰ Срок сдачи", assignment.dueDate.take(10), isLast = true)
                    }
                }

                // Attachment
                if (assignment.hasAttachment) {
                    item {
                        InfoCard {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Orange.copy(alpha = 0.1f))
                                    .border(1.dp, Orange.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("📎", fontSize = 20.sp)
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text(assignment.attachmentName ?: "Файл", color = TextWhite, fontSize = 13.sp)
                                    Text("Прикреплённый файл", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Submissions
                item {
                    Text(
                        "Сдали (${assignment.studentSubmissions.count { it.submission != null }}/${assignment.studentSubmissions.size})",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(assignment.studentSubmissions) { sub ->
                    SubmissionRow(sub)
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardDark,
            shape = RoundedCornerShape(16.dp),
            title = { Text("Удалить задание?", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Это действие нельзя отменить", color = TextMuted, fontSize = 14.sp) },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Удалить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Отмена", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SubmissionRow(sub: StudentSubmissionDto) {
    val hasSub = sub.submission != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark)
            .border(1.dp, if (hasSub) Green.copy(alpha = 0.3f) else Color(0xFF2A2A38), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (hasSub) Green.copy(alpha = 0.15f) else Color(0xFF1A1A28)),
            contentAlignment = Alignment.Center
        ) {
            Text(sub.studentName.firstOrNull()?.toString() ?: "?",
                color = if (hasSub) Green else TextMuted,
                fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(sub.studentName, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("@${sub.studentLogin}", color = TextMuted, fontSize = 11.sp)
        }
        if (hasSub) {
            val score = sub.submission?.score
            if (score != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Green.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("$score", color = Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("✅ Сдал", color = Green, fontSize = 12.sp)
            }
        } else {
            Text("⏳ Ожидание", color = TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(12.dp))
            .padding(14.dp),
        content = content
    )
}

@Composable
private fun DetailRow(label: String, value: String, isLast: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.width(120.dp))
        Text(value, color = TextWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
    if (!isLast) HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
}

// ── Create dialog ─────────────────────────────────────────────────────────────
@Composable
private fun CreateAssignmentDialog(
    groups: List<GroupDto>,
    isSaving: Boolean,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedGroupId by remember { mutableStateOf(groups.firstOrNull()?.id ?: "") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var groupExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Новое задание", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Group selector
                Text("Группа", color = TextMuted, fontSize = 12.sp)
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceDark)
                            .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(8.dp))
                            .clickable { groupExpanded = true }
                            .padding(12.dp)
                    ) {
                        Text(
                            groups.find { it.id == selectedGroupId }?.name ?: "Выберите группу",
                            color = TextWhite,
                            fontSize = 13.sp
                        )
                    }
                    DropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false },
                        modifier = Modifier.background(CardDark)
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name, color = TextWhite, fontSize = 13.sp) },
                                onClick = { selectedGroupId = group.id; groupExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание задания", fontSize = 12.sp) },
                    minLines = 2,
                    colors = fieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Срок сдачи (YYYY-MM-DD)", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = fieldColors(),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedGroupId, description, dueDate) },
                enabled = !isSaving && selectedGroupId.isNotBlank() && description.isNotBlank() && dueDate.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Создать", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = TextMuted) }
        }
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = SurfaceDark,
    unfocusedContainerColor = SurfaceDark,
    focusedBorderColor = Blue,
    unfocusedBorderColor = Color(0xFF2A2A38),
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedLabelColor = Blue,
    unfocusedLabelColor = TextMuted
)