package org.example.project.feature.schedule.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.models.Lesson
import org.example.project.feature.schedule.ScheduleComponent
import org.example.project.feature.schedule.ScheduleIntent
import org.example.project.feature.schedule.ViewMode
import kotlinx.datetime.*
import org.example.project.data.remote.dto.AttendanceMarkDto
import org.example.project.data.remote.dto.LessonDto
import org.example.project.data.remote.dto.StudentInLessonDto
import org.example.project.core.ui.theme.*

private val accentColors = listOf(PurpleAccent, TealAccent, AmberAccent, BlueAccent, PinkAccent)

private fun accentFor(name: String): Color {
    val idx = kotlin.math.abs(name.hashCode()) % accentColors.size
    return accentColors[idx]
}

@Composable
fun ScheduleScreen(component: ScheduleComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(BgDark)
    ) {
        ScheduleHeader(lessonCount = state.lessons.size)

        ViewModeTabs(
            selected = state.viewMode,
            onSelect = { component.handleIntent(ScheduleIntent.SetViewMode(it)) }
        )

        when (state.viewMode) {
            ViewMode.MONTH -> {
                MonthSection(
                    selectedDate = state.selectedDate,
                    onPrevMonth = { component.handleIntent(ScheduleIntent.PrevWeek) },
                    onNextMonth = { component.handleIntent(ScheduleIntent.NextWeek) },
                    onDateSelected = { component.handleIntent(ScheduleIntent.SelectDate(it)) }
                )
            }
            else -> {
                WeekSection(
                    calendarDays = state.calendarDays,
                    selectedDate = state.selectedDate,
                    onPrevWeek = { component.handleIntent(ScheduleIntent.PrevWeek) },
                    onNextWeek = { component.handleIntent(ScheduleIntent.NextWeek) },
                    onDateSelected = { component.handleIntent(ScheduleIntent.SelectDate(it)) }
                )
            }
        }

        if (state.isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PurpleAccent, modifier = Modifier.size(28.dp))
            }
        } else {
            when (state.viewMode) {
                ViewMode.LIST -> LessonListView(
                    modifier = Modifier.weight(1f),
                    lessons = state.lessons,
                    selectedDate = state.selectedDate
                )
                ViewMode.DAY -> DayView(
                    modifier = Modifier.weight(1f),
                    lessons = state.lessons,
                    selectedDate = state.selectedDate
                )
                else -> LessonTimelineView(
                    modifier = Modifier.weight(1f),
                    lessons = state.lessons,
                    selectedDate = state.selectedDate
                )
            }
        }
    }
    AnimatedVisibility(
        visible = state.selectedLesson != null || state.isLessonLoading,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        LessonDetailSheet(
            lesson = state.selectedLesson,
            isLoading = state.isLessonLoading,
            isSaving = state.isSavingAttendance,
            userRole = component.userRole,
            onClose = { component.handleIntent(ScheduleIntent.CloseLesson) },
            onSaveAttendance = { component.handleIntent(ScheduleIntent.SaveAttendance(it)) }
        )
    }
}
@Composable
private fun LessonDetailSheet(
    lesson: LessonDto?,
    isLoading: Boolean,
    isSaving: Boolean,
    userRole: Int,
    onClose: () -> Unit,
    onSaveAttendance: (List<AttendanceMarkDto>) -> Unit
) {
    // attendance state для учителя
    val attendanceMap = remember(lesson?.id) {
        mutableStateMapOf<String, Int>().also { map ->
            lesson?.students?.forEach { s ->
                map[s.id] = s.attendanceStatus ?: 1
            }
        }
    }

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
                .fillMaxHeight(if (userRole == 3 && lesson != null) 0.92f else 0.6f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceDark)
                .clickable(enabled = false) {}
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2A2A38))
                    .align(Alignment.CenterHorizontally)
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PurpleAccent)
                }
                return@Column
            }

            lesson ?: return@Column

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF3D1F8C), PurpleAccent))
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        lesson.subject?.subjectName ?: "—",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${lesson.startTime.take(5)} – ${lesson.endTime.take(5)}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
                // Статус урока
                val statusColor = when (lesson.lessonStatus) {
                    "Completed" -> TealAccent
                    "Cancelled" -> Color(0xFFEF4444)
                    else -> AmberAccent
                }
                val statusText = when (lesson.lessonStatus) {
                    "Completed" -> "Завершён"
                    "Cancelled" -> "Отменён"
                    else -> "Запланирован"
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Основная инфо
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardDark)
                            .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(12.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LessonInfoRow("👨‍🏫 Преподаватель", lesson.teacher?.name ?: "—")
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
                        LessonInfoRow("🚪 Кабинет", lesson.room?.name ?: "—")
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
                        LessonInfoRow("👥 Группа", lesson.group?.name ?: "—")
                        if (lesson.note != null) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
                            LessonInfoRow("📌 Заметка", lesson.note)
                        }
                        if (lesson.cancelReason != null) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
                            LessonInfoRow("❌ Причина отмены", lesson.cancelReason)
                        }
                    }
                }

                // Для студента — его статус посещаемости
                if (userRole == 1 && lesson.students.isNotEmpty()) {
                    item {
                        val student = lesson.students.first()
                        val statusColor = when (student.attendanceStatus) {
                            1 -> TealAccent
                            2 -> Color(0xFFEF4444)
                            3 -> AmberAccent
                            else -> TextMuted
                        }
                        val statusText = when (student.attendanceStatus) {
                            1 -> "✅ Присутствовал"
                            2 -> "❌ Отсутствовал"
                            3 -> "⏰ Опоздал"
                            else -> "⏳ Ещё не отмечен"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor.copy(alpha = 0.1f))
                                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Text(statusText, color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Для учителя — список студентов с отметкой
                if (userRole == 3 && lesson.students.isNotEmpty()) {
                    item {
                        Text(
                            "Студенты (${lesson.students.size})",
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(lesson.students) { student ->
                        StudentAttendanceRow(
                            student = student,
                            selectedStatus = attendanceMap[student.id] ?: 1,
                            onStatusChange = { attendanceMap[student.id] = it }
                        )
                    }

                    // Кнопка сохранить
                    if (lesson.lessonStatus != "Completed") {
                        item {
                            Button(
                                onClick = {
                                    val list = attendanceMap.map { (studentId, status) ->
                                        AttendanceMarkDto(studentId = studentId, status = status)
                                    }
                                    onSaveAttendance(list)
                                },
                                enabled = !isSaving,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text("Сохранить посещаемость", color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp, modifier = Modifier.width(140.dp))
        Text(value, color = TextWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StudentAttendanceRow(
    student: StudentInLessonDto,
    selectedStatus: Int,
    onStatusChange: (Int) -> Unit
) {
    val statusOptions = listOf(
        1 to ("✅" to TealAccent),
        2 to ("❌" to Color(0xFFEF4444)),
        3 to ("⏰" to AmberAccent),
        4 to ("📋" to BlueAccent)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CardDark)
            .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(PurpleAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                student.fullName.firstOrNull()?.toString() ?: "?",
                color = PurpleAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))

        // Имя + оставшиеся уроки
        Column(modifier = Modifier.weight(1f)) {
            Text(student.fullName, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (student.remainingLessons > 0) {
                Text("Осталось уроков: ${student.remainingLessons}", color = AmberAccent, fontSize = 10.sp)
            } else {
                Text("Уроки закончились", color = Color(0xFFEF4444), fontSize = 10.sp)
            }
        }

        // Кнопки статуса
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            statusOptions.forEach { (status, pair) ->
                val (emoji, color) = pair
                val isSelected = selectedStatus == status
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF1A1A28))
                        .border(
                            1.dp,
                            if (isSelected) color else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onStatusChange(status) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ScheduleHeader(lessonCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF3D1F8C), PurpleAccent))
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Расписание",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$lessonCount занятий сегодня",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("ДТ", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ViewModeTabs(selected: ViewMode, onSelect: (ViewMode) -> Unit) {
    val tabs = listOf("День" to ViewMode.DAY, "Неделя" to ViewMode.WEEK, "Месяц" to ViewMode.MONTH, "Список" to ViewMode.LIST)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(CardDark)
                .padding(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                tabs.forEach { (label, mode) ->
                    val isOn = selected == mode
                    val bg by animateColorAsState(
                        if (isOn) PurpleAccent else Color.Transparent,
                        animationSpec = tween(200)
                    )
                    val textColor by animateColorAsState(
                        if (isOn) Color.White else TextMuted,
                        animationSpec = tween(200)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable { onSelect(mode) }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = textColor, fontSize = 12.sp, fontWeight = if (isOn) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekSection(
    calendarDays: List<LocalDate>,
    selectedDate: LocalDate,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().background(SurfaceDark)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevWeek,
                modifier = Modifier.size(28.dp).clip(CircleShape).background(CardDark)
            ) {
                Icon(Icons.Default.KeyboardArrowLeft, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
            if (calendarDays.size == 7) {
                val s = calendarDays.first(); val e = calendarDays.last()
                Text(
                    "${s.dayOfMonth} ${monthShort(s.month)} — ${e.dayOfMonth} ${monthShort(e.month)}",
                    color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium
                )
            }
            IconButton(
                onClick = onNextWeek,
                modifier = Modifier.size(28.dp).clip(CircleShape).background(CardDark)
            ) {
                Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val dayNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
            calendarDays.forEachIndexed { idx, date ->
                val isSelected = date == selectedDate
                val isToday = date == today
                val bgNum by animateColorAsState(if (isToday) PurpleAccent else if (isSelected) CardDark else Color.Transparent)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected && !isToday) Color(0xFF1E1E2E) else Color.Transparent)
                        .clickable { onDateSelected(date) }
                        .padding(vertical = 5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(dayNames.getOrElse(idx) { "" }, color = TextDim, fontSize = 10.sp)
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.size(28.dp).clip(CircleShape).background(bgNum),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            date.dayOfMonth.toString(),
                            color = if (isToday) Color.White else if (isSelected) TextWhite else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSection(
    selectedDate: LocalDate,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    val firstDay = LocalDate(selectedDate.year, selectedDate.month, 1)
    val daysInMonth = when (selectedDate.month) {
        Month.FEBRUARY -> if (isLeapYear(selectedDate.year)) 29 else 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        else -> 31
    }
    val startOffset = firstDay.dayOfWeek.ordinal // Mon=0

    Column(modifier = Modifier.fillMaxWidth().background(SurfaceDark).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp).clip(CircleShape).background(CardDark)) {
                Icon(Icons.Default.KeyboardArrowLeft, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
            Text(
                "${monthFull(selectedDate.month)} ${selectedDate.year}",
                color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
            )
            IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp).clip(CircleShape).background(CardDark)) {
                Icon(Icons.Default.KeyboardArrowRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Пн","Вт","Ср","Чт","Пт","Сб","Вс").forEach { d ->
                Text(d, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = TextDim, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(6.dp))

        val cells = mutableListOf<LocalDate?>()
        repeat(startOffset) { cells.add(null) }
        for (d in 1..daysInMonth) cells.add(LocalDate(selectedDate.year, selectedDate.month, d))
        while (cells.size % 7 != 0) cells.add(null)

        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    if (date == null) {
                        Box(modifier = Modifier.weight(1f).height(36.dp))
                    } else {
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .clip(CircleShape)
                                .background(if (isToday) PurpleAccent else if (isSelected) PurpleAccent.copy(0.25f) else Color.Transparent)
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                date.dayOfMonth.toString(),
                                color = if (isToday) Color.White else if (isSelected) PurpleAccent else TextMuted,
                                fontSize = 13.sp,
                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun LessonTimelineView(modifier: Modifier, lessons: List<Lesson>, selectedDate: LocalDate) {
    if (lessons.isEmpty()) {
        EmptyState(modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${selectedDate.dayOfMonth} ${monthShort(selectedDate.month)}, ${dayOfWeekFull(selectedDate.dayOfWeek)}",
                    color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
            }
            items(lessons) { LessonCard(it) }
        }
    }
}

@Composable
private fun DayView(modifier: Modifier, lessons: List<Lesson>, selectedDate: LocalDate) {
    if (lessons.isEmpty()) {
        EmptyState(modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Text(
                    "${selectedDate.dayOfMonth} ${monthShort(selectedDate.month)}",
                    color = TextMuted, fontSize = 11.sp
                )
                Spacer(Modifier.height(8.dp))
            }
            items(lessons) { lesson ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(
                        modifier = Modifier.width(42.dp).padding(top = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(lesson.startTime, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(2.dp))
                        Box(modifier = Modifier.width(1.dp).height(60.dp).background(Color(0xFF252530)))
                        Spacer(Modifier.height(2.dp))
                        Text(lesson.endTime, color = TextDim, fontSize = 10.sp)
                    }
                    Spacer(Modifier.width(10.dp))
                    LessonCard(lesson, modifier = Modifier.weight(1f).padding(bottom = 12.dp))
                }
            }
        }
    }
}

@Composable
private fun LessonListView(modifier: Modifier, lessons: List<Lesson>, selectedDate: LocalDate) {
    if (lessons.isEmpty()) {
        EmptyState(modifier)
    } else {
        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Все занятия • ${lessons.size}",
                    color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(6.dp))
            }
            items(lessons) { lesson ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentFor(lesson.subjectName).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            lesson.subjectName.take(1),
                            color = accentFor(lesson.subjectName),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lesson.subjectName, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(lesson.teacherName, color = TextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${lesson.startTime}–${lesson.endTime}", color = accentFor(lesson.subjectName), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(2.dp))
                        Text(lesson.roomName, color = TextDim, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null ) {
    val accent = accentFor(lesson.subjectName)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF13131A))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ){

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    lesson.subjectName,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                // Бейдж времени
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.18f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        "${lesson.startTime}–${lesson.endTime}",
                        color = accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Аватар + имя преподавателя
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(lesson.teacherName.take(1), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(lesson.teacherName, color = TextMuted, fontSize = 11.sp)
                }

                // Кабинет
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1E2A))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text("🚪 ${lesson.roomName}", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }

            if (lesson.groupName.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1A1A28))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text("👥 ${lesson.groupName}", color = TextDim, fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📭", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text("Занятий нет", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("На этот день ничего не запланировано", color = TextDim, fontSize = 11.sp)
        }
    }
}

private fun isLeapYear(year: Int) = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0

private fun monthShort(month: Month): String = when (month) {
    Month.JANUARY -> "янв"; Month.FEBRUARY -> "фев"; Month.MARCH -> "мар"
    Month.APRIL -> "апр"; Month.MAY -> "май"; Month.JUNE -> "июн"
    Month.JULY -> "июл"; Month.AUGUST -> "авг"; Month.SEPTEMBER -> "сен"
    Month.OCTOBER -> "окт"; Month.NOVEMBER -> "ноя"; Month.DECEMBER -> "дек"
    else -> ""
}

private fun monthFull(month: Month): String = when (month) {
    Month.JANUARY -> "Январь"; Month.FEBRUARY -> "Февраль"; Month.MARCH -> "Март"
    Month.APRIL -> "Апрель"; Month.MAY -> "Май"; Month.JUNE -> "Июнь"
    Month.JULY -> "Июль"; Month.AUGUST -> "Август"; Month.SEPTEMBER -> "Сентябрь"
    Month.OCTOBER -> "Октябрь"; Month.NOVEMBER -> "Ноябрь"; Month.DECEMBER -> "Декабрь"
    else -> ""
}

private fun dayOfWeekFull(dow: DayOfWeek): String = when (dow) {
    DayOfWeek.MONDAY -> "понедельник"; DayOfWeek.TUESDAY -> "вторник"
    DayOfWeek.WEDNESDAY -> "среда"; DayOfWeek.THURSDAY -> "четверг"
    DayOfWeek.FRIDAY -> "пятница"; DayOfWeek.SATURDAY -> "суббота"
    DayOfWeek.SUNDAY -> "воскресенье"; else -> ""
}