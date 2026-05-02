package org.example.project.feature.teacher.attendance.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import org.example.project.core.ui.theme.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.remote.dto.AttendanceGroupStatsDto
import org.example.project.data.remote.dto.AttendanceRecordDto
import org.example.project.data.remote.dto.GroupDto
import org.example.project.data.remote.dto.StudentAttendanceStatsDto
import org.example.project.feature.teacher.AttendanceTab
import org.example.project.feature.teacher.TeacherAttendanceComponent
import org.example.project.feature.teacher.components.HeaderAvatar

@Composable
fun TeacherAttendanceScreen(component: TeacherAttendanceComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF065F46), Color(0xFF059669))))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Посещаемость", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Отслеживание и анализ посещаемости студентов",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                    HeaderAvatar(initials = state.userInitials, onLogout = component.onLogout)
                }

                Spacer(Modifier.height(16.dp))

                // Stats row — остаётся без изменений
                val totalRecords = state.attendances.size
                val present = state.attendances.count { it.status == 1 }
                val absent = state.attendances.count { it.status == 2 }
                val rate = if (totalRecords > 0) (present * 100 / totalRecords) else 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip("$rate%", "Посещаемость", Green)
                    StatChip("$present", "Присутствовали", Green)
                    StatChip("$absent", "Пропустили", Red)
                    StatChip("$totalRecords", "Всего записей", Blue)
                }
            }
        }
        // ── Tabs ──────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AttendanceTabButton(
                label = "По группам",
                isActive = state.activeTab == AttendanceTab.BY_GROUPS,
                onClick = { component.selectTab(AttendanceTab.BY_GROUPS) },
                modifier = Modifier.weight(1f)
            )
            AttendanceTabButton(
                label = "По студентам",
                isActive = state.activeTab == AttendanceTab.BY_STUDENTS,
                onClick = { component.selectTab(AttendanceTab.BY_STUDENTS) },
                modifier = Modifier.weight(1f)
            )
        }

        // ── Filters ───────────────────────────────────────────────────────────
        FiltersRow(
            groups = state.groups,
            selectedGroupId = state.selectedGroupId,
            selectedStatus = state.selectedStatus,
            onGroupSelected = { component.selectGroup(it) },
            onStatusSelected = { component.selectStatus(it) }
        )

        // ── Content ───────────────────────────────────────────────────────────
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green)
            }
        } else {
            when (state.activeTab) {
                AttendanceTab.BY_GROUPS -> GroupsContent(
                    stats = state.groupStats,
                    onGroupClick = { component.selectGroup(it.groupId) }
                )
                AttendanceTab.BY_STUDENTS -> StudentsContent(
                    attendances = state.attendances,
                    groupDetails = state.groupDetails?.students ?: emptyList()
                )
            }
        }
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
private fun StatChip(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp)
    }
}

// ── Tab button ────────────────────────────────────────────────────────────────
@Composable
private fun AttendanceTabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) Green.copy(alpha = 0.2f) else Color(0xFF1A1A28))
            .border(1.dp, if (isActive) Green.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (isActive) Green else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Filters ───────────────────────────────────────────────────────────────────
@Composable
private fun FiltersRow(
    groups: List<GroupDto>,
    selectedGroupId: String?,
    selectedStatus: Int?,
    onGroupSelected: (String?) -> Unit,
    onStatusSelected: (Int?) -> Unit
) {
    var groupExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        null to "Все статусы",
        1 to "Присутствовал",
        2 to "Отсутствовал",
        3 to "Опоздал",
        4 to "Уваж. причина"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Group dropdown
        Box(modifier = Modifier.weight(1f)) {
            FilterChip(
                label = groups.find { it.id == selectedGroupId }?.name ?: "Все группы",
                onClick = { groupExpanded = true }
            )
            DropdownMenu(
                expanded = groupExpanded,
                onDismissRequest = { groupExpanded = false },
                modifier = Modifier.background(CardDark)
            ) {
                DropdownMenuItem(
                    text = { Text("Все группы", color = TextWhite, fontSize = 13.sp) },
                    onClick = { onGroupSelected(null); groupExpanded = false }
                )
                groups.forEach { group ->
                    DropdownMenuItem(
                        text = { Text(group.name, color = TextWhite, fontSize = 13.sp) },
                        onClick = { onGroupSelected(group.id); groupExpanded = false }
                    )
                }
            }
        }

        // Status dropdown
        Box(modifier = Modifier.weight(1f)) {
            FilterChip(
                label = statusOptions.find { it.first == selectedStatus }?.second ?: "Все статусы",
                onClick = { statusExpanded = true }
            )
            DropdownMenu(
                expanded = statusExpanded,
                onDismissRequest = { statusExpanded = false },
                modifier = Modifier.background(CardDark)
            ) {
                statusOptions.forEach { (status, label) ->
                    DropdownMenuItem(
                        text = { Text(label, color = TextWhite, fontSize = 13.sp) },
                        onClick = { onStatusSelected(status); statusExpanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(CardDark)
            .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(label, color = TextWhite, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ── Groups content ────────────────────────────────────────────────────────────
@Composable
private fun GroupsContent(
    stats: List<AttendanceGroupStatsDto>,
    onGroupClick: (AttendanceGroupStatsDto) -> Unit
) {
    if (stats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📊", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Нет данных по группам", color = TextWhite, fontSize = 16.sp)
            }
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(stats) { group ->
            GroupStatsCard(group = group, onClick = { onGroupClick(group) })
        }
    }
}

@Composable
private fun GroupStatsCard(group: AttendanceGroupStatsDto, onClick: () -> Unit) {
    val rate = group.attendanceRate
    val rateColor = when {
        rate >= 80 -> Green
        rate >= 60 -> Orange
        else -> Red
    }
    val trendColor = if (group.trend >= 0) Green else Red
    val trendSign = if (group.trend >= 0) "↑" else "↓"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, rateColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(group.groupName, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(group.subjectName, color = TextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${rate.toInt()}%", color = rateColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("$trendSign ${kotlin.math.abs(group.trend).toInt()}%", color = trendColor, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFF1A1A28))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(rate.toFloat() / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(rateColor)
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            MiniStat("👥 ${group.totalStudents}", "студентов")
            MiniStat("✅ ${group.attendedCount}", "посетили")
            MiniStat("❌ ${group.missedCount}", "пропустили")
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column {
        Text(value, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

// ── Students content ──────────────────────────────────────────────────────────
@Composable
private fun StudentsContent(
    attendances: List<AttendanceRecordDto>,
    groupDetails: List<StudentAttendanceStatsDto>
) {
    if (groupDetails.isNotEmpty()) {
        // Show per-student stats when a group is selected
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(groupDetails) { student ->
                StudentStatsCard(student)
            }
        }
    } else if (attendances.isNotEmpty()) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attendances) { record ->
                AttendanceRecordCard(record)
            }
        }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("👤", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Выберите группу для просмотра", color = TextWhite, fontSize = 15.sp)
                Text("статистики по студентам", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StudentStatsCard(student: StudentAttendanceStatsDto) {
    val rate = student.attendanceRate
    val rateColor = when {
        rate >= 80 -> Green
        rate >= 60 -> Orange
        else -> Red
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, rateColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(rateColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                student.studentName.firstOrNull()?.toString() ?: "?",
                color = rateColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(student.studentName, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("@${student.studentLogin}", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MiniStatusBadge("✅ ${student.attendedLessons}", Green)
                MiniStatusBadge("❌ ${student.missedLessons}", Red)
                MiniStatusBadge("⏰ ${student.lateLessons}", Orange)
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("${rate.toInt()}%", color = rateColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("${student.totalLessons} уроков", color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MiniStatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 10.sp)
    }
}

@Composable
private fun AttendanceRecordCard(record: AttendanceRecordDto) {
    val statusColor = when (record.status) {
        1 -> Green
        2 -> Red
        3 -> Orange
        else -> Blue
    }
    val statusEmoji = when (record.status) {
        1 -> "✅"
        2 -> "❌"
        3 -> "⏰"
        else -> "📋"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(statusColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(record.studentName.firstOrNull()?.toString() ?: "?", color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(record.studentName, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text("${record.subjectName} • ${record.groupName}", color = TextMuted, fontSize = 11.sp)
            Text("${record.date} • ${record.startTime.take(5)}–${record.endTime.take(5)}", color = TextMuted, fontSize = 10.sp)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(statusEmoji, fontSize = 18.sp)
            Text(record.statusName, color = statusColor, fontSize = 10.sp)
        }
    }
}