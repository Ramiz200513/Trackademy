
package org.example.project.feature.teacher.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.remote.dto.TodayLessonDto
import org.example.project.feature.teacher.dashboard.DashboardComponent
import org.example.project.core.ui.theme.*
import org.example.project.feature.teacher.components.HeaderAvatar

@Composable
fun DashboardScreen(component: DashboardComponent) {
    val state by component.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Purple
                )
            }
            state.error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Ошибка загрузки", color = TextWhite, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { component.load() },
                        colors = ButtonDefaults.buttonColors(containerColor = Purple)
                    ) { Text("Повторить") }
                }
            }
            state.data != null -> {
                val data = state.data!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Header
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(Color(0xFF4C1D95), Color(0xFF6C3FCF))))
                                .padding(horizontal = 20.dp, vertical = 24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Статистика",
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Сводка за сегодня",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 13.sp
                                    )
                                }
                                HeaderAvatar(initials = "ИО", onLogout = component.onLogout)
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "👥",
                                value = "${data.totalGroups}",
                                label = "Групп",
                                color = Blue
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "📚",
                                value = "${data.lessonsToday}",
                                label = "Уроков сегодня",
                                color = Green
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                emoji = "📝",
                                value = "${data.ungradedSubmissions}",
                                label = "Не проверено",
                                color = if (data.ungradedSubmissions > 0) Orange else Green
                            )
                        }
                    }

                    // Today schedule title
                    item {
                        Text(
                            "Расписание на сегодня",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    if (data.todaySchedule.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CardDark)
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎉", fontSize = 32.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Уроков сегодня нет",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(data.todaySchedule) { lesson ->
                            TodayLessonCard(
                                lesson = lesson,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(label, color = TextMuted, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
private fun TodayLessonCard(lesson: TodayLessonDto, modifier: Modifier = Modifier) {
    val borderColor = if (lesson.isPast) TextMuted.copy(alpha = 0.2f) else Purple.copy(alpha = 0.4f)
    val timeColor = if (lesson.isPast) TextMuted else Purple

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time
            Column {
                Text(
                    "${lesson.startTime.take(5)} – ${lesson.endTime.take(5)}",
                    color = timeColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                if (lesson.isPast) {
                    Text("Завершён", color = TextMuted, fontSize = 10.sp)
                } else {
                    Text("Предстоит", color = Green, fontSize = 10.sp)
                }
            }

            // Attendance badge
            if (lesson.presentCount != null && lesson.totalStudents != null) {
                val rate = if (lesson.totalStudents > 0)
                    lesson.presentCount * 100 / lesson.totalStudents else 0
                val rateColor = when {
                    rate >= 80 -> Green
                    rate >= 50 -> Orange
                    else -> Red
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${lesson.presentCount}/${lesson.totalStudents}",
                        color = rateColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("присутствуют", color = TextMuted, fontSize = 10.sp)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
        Spacer(Modifier.height(10.dp))

        Text(lesson.subjectName, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("👥 ${lesson.groupName}", color = TextMuted, fontSize = 12.sp)
            Text("🚪 ${lesson.roomName}", color = TextMuted, fontSize = 12.sp)
        }
    }
}