package org.example.project.feature.student.perfomance.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import org.example.project.domain.models.Assignment
import org.example.project.domain.models.Grade
import org.example.project.feature.performance.PerformanceComponent
import org.example.project.core.ui.theme.*


private fun accentFor(name: String) = accentColors[kotlin.math.abs(name.hashCode()) % accentColors.size]

@Composable
fun PerformanceScreen(component: PerformanceComponent) {
    val state by component.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(BgDark)
    ) {
        // --- Шапка ---
          Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF3D1F8C), PurpleAccent)))
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp).padding(top = 16.dp, bottom = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Успеваемость", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(state.userName, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.userInitials,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard("${state.avgGrade}", "Средний балл", Modifier.weight(1f))
                    StatCard("${state.attendance}%", "Посещаемость", Modifier.weight(1f))
                    StatCard("${state.activeCount}", "Задания", Modifier.weight(1f))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
        ) {
            item {
                SectionTitle("Активные задания", state.assignments.size.toString())
                Spacer(Modifier.height(10.dp))
            }
            items(state.assignments) { assignment ->
                AssignmentCard(assignment)
                Spacer(Modifier.height(8.dp))
            }
            item {
                Spacer(Modifier.height(8.dp))
                SectionTitle("Последние оценки", "")
                Spacer(Modifier.height(10.dp))
            }
            items(state.grades) { grade ->
                GradeRow(grade)
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column {
            Text(value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        if (count.isNotEmpty() && count != "0") {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(PurpleAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(count, color = PurpleAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AssignmentCard(assignment: Assignment) {
    val accent = accentFor(assignment.subjectName)
    val isOverdue = assignment.isOverdue
    val statusColor = if (isOverdue) Color(0xFFEF4444) else Color(0xFF10B981)
    val statusText = if (isOverdue) "Просрочено" else assignment.status

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF13131A))
                .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(14.dp)) // цветная рамка
        )
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
                    assignment.subjectName,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(statusText, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("до ${assignment.dueDate.take(10)}", color = TextMuted, fontSize = 11.sp)
                if (assignment.description.isNotBlank()) {
                    Text(" · ", color = TextDim, fontSize = 11.sp)
                    Text(assignment.description, color = TextDim, fontSize = 11.sp, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun GradeRow(grade: Grade) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accentFor(grade.subjectName).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                grade.subjectName.take(1),
                color = accentFor(grade.subjectName),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(grade.subjectName, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(grade.date.take(10), color = TextMuted, fontSize = 11.sp)
        }
        Text(
            text = grade.grade.toString(),
            color = when {
                grade.grade >= 90 -> Color(0xFF10B981)
                grade.grade < 60 -> Color(0xFFEF4444)
                else -> Color(0xFFF59E0B)
            },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
}