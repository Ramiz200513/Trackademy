package org.example.project.root.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import org.example.project.feature.student.assignment.ui.AssignmentScreen
import org.example.project.feature.student.materials.ui.MaterialScreen
import org.example.project.feature.student.perfomance.ui.PerformanceScreen
import org.example.project.feature.profile.ui.ProfileScreen
import org.example.project.feature.schedule.ui.ScheduleScreen
import org.example.project.feature.teacher.assignment.ui.TeacherAssignmentScreen
import org.example.project.feature.teacher.attendance.ui.TeacherAttendanceScreen
import org.example.project.feature.teacher.dashboard.ui.DashboardScreen
import org.example.project.root.MainComponent
import org.example.project.core.ui.theme.*


@Composable
fun MainContent(component: MainComponent) {
    val stack by component.stack.subscribeAsState()
    val activeTab = when (stack.active.instance) {
        is MainComponent.MainChild.Schedule         -> MainComponent.MainTab.SCHEDULE
        is MainComponent.MainChild.Performance      -> MainComponent.MainTab.PERFORMANCE
        is MainComponent.MainChild.Assignments      -> MainComponent.MainTab.HOME
        is MainComponent.MainChild.Materials        -> MainComponent.MainTab.MATERIALS
        is MainComponent.MainChild.Profile          -> MainComponent.MainTab.PROFILE
        is MainComponent.MainChild.TeacherAttendance   -> MainComponent.MainTab.ATTENDANCE
        is MainComponent.MainChild.TeacherAssignments  -> MainComponent.MainTab.HOME
        else -> MainComponent.MainTab.SCHEDULE
    }

    Scaffold(
        containerColor = Color(0xFF0F0F14),
        bottomBar = {
            AppBottomBar(
                activeTab = activeTab,
                userRole = component.userRole,
                onTabClick = { tab -> component.onTabClick(tab) }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Children(stack = component.stack, animation = stackAnimation(fade())) { child ->
                when (val instance = child.instance) {
                    is MainComponent.MainChild.Schedule          -> ScheduleScreen(instance.component)
                    is MainComponent.MainChild.Performance       -> PerformanceScreen(instance.component)
                    is MainComponent.MainChild.Assignments       -> AssignmentScreen(instance.component)
                    is MainComponent.MainChild.Materials         -> MaterialScreen(instance.component)
                    is MainComponent.MainChild.Profile           -> ProfileScreen(instance.component)
                    is MainComponent.MainChild.TeacherAttendance -> TeacherAttendanceScreen(instance.component)
                    is MainComponent.MainChild.TeacherAssignments -> TeacherAssignmentScreen(instance.component)
                    is MainComponent.MainChild.Dashboard -> DashboardScreen(instance.component)
                }
            }
        }
    }
}

@Composable
private fun AppBottomBar(
    activeTab: MainComponent.MainTab,
    userRole: Int,
    onTabClick: (MainComponent.MainTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(BarBg)) {
        HorizontalDivider(thickness = 0.5.dp, color = BarDivider)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (userRole == 1) {
                // ── Студент ───────────────────────────────────────────────────
                NavItem(NavIcon.Statistics, "Статистика",
                    activeTab == MainComponent.MainTab.PERFORMANCE)
                { onTabClick(MainComponent.MainTab.PERFORMANCE) }
                NavItem(NavIcon.Calendar, "Расписание",
                    activeTab == MainComponent.MainTab.SCHEDULE)
                { onTabClick(MainComponent.MainTab.SCHEDULE) }
                NavItem(NavIcon.Homework, "Задания",
                    activeTab == MainComponent.MainTab.HOME)
                { onTabClick(MainComponent.MainTab.HOME) }
                NavItem(NavIcon.Materials, "Материалы",
                    activeTab == MainComponent.MainTab.MATERIALS)
                { onTabClick(MainComponent.MainTab.MATERIALS) }
                NavItem(NavIcon.Profile, "Профиль",
                    activeTab == MainComponent.MainTab.PROFILE)
                { onTabClick(MainComponent.MainTab.PROFILE) }
            } else {
                // ── Учитель / Админ ───────────────────────────────────────────
                NavItem(NavIcon.Statistics, "Статистика",
                    activeTab == MainComponent.MainTab.PERFORMANCE)
                { onTabClick(MainComponent.MainTab.PERFORMANCE) }
                NavItem(NavIcon.Calendar, "Расписание",
                    activeTab == MainComponent.MainTab.SCHEDULE)
                { onTabClick(MainComponent.MainTab.SCHEDULE) }
                NavItem(NavIcon.Attendance, "Посещ-ть",
                    activeTab == MainComponent.MainTab.ATTENDANCE)
                { onTabClick(MainComponent.MainTab.ATTENDANCE) }
                NavItem(NavIcon.Homework, "Задания",
                    activeTab == MainComponent.MainTab.HOME)
                { onTabClick(MainComponent.MainTab.HOME) }
                NavItem(NavIcon.Materials, "Материалы",
                    activeTab == MainComponent.MainTab.MATERIALS)
                { onTabClick(MainComponent.MainTab.MATERIALS) }
            }
        }
    }
}
sealed class NavIcon {
    object Attendance  : NavIcon()
    object Statistics : NavIcon()
    object Calendar : NavIcon()
    object Homework : NavIcon()
    object Materials : NavIcon()
    object Library : NavIcon()
    object Profile : NavIcon()
}

@Composable
private fun NavItem(
    icon: NavIcon,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val color = if (isActive) NavPurple else NavInactive
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NavIconDrawable(icon = icon, color = color)
        Spacer(Modifier.height(3.dp))
        Text(label, color = color, fontSize = 9.sp, maxLines = 1)
        Spacer(Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .size(width = 16.dp, height = 2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(if (isActive) NavPurple else Color.Transparent)
        )
    }
}

@Composable
private fun NavIconDrawable(icon: NavIcon, color: Color) {
    val size = Modifier.size(22.dp)
    when (icon) {
        is NavIcon.Attendance -> Canvas(modifier = size) {
            val w = this.size.width; val h = this.size.height
            drawCircle(color, radius = w * 0.35f,
                center = Offset(w * 0.5f, h * 0.45f),
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawLine(color,
                Offset(w * 0.5f, h * 0.28f),
                Offset(w * 0.5f, h * 0.45f),
                strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w * 0.5f, h * 0.45f),
                Offset(w * 0.65f, h * 0.55f),
                strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w * 0.2f, h * 0.82f),
                Offset(w * 0.8f, h * 0.82f),
                strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
        }
        is NavIcon.Statistics -> Canvas(modifier = size) {
            val w = this.size.width; val h = this.size.height
            val paint = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w*0.1f, h*0.7f),
                Offset(w*0.3f, h*0.4f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w*0.3f, h*0.4f),
                Offset(w*0.55f, h*0.6f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w*0.55f, h*0.6f),
                Offset(w*0.9f, h*0.2f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w*0.1f, h*0.85f),
                Offset(w*0.9f, h*0.85f), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
        }
        is NavIcon.Calendar -> Canvas(modifier = size) {
            val w = this.size.width; val h = this.size.height
            val r = 1.5.dp.toPx()
            drawRoundRect(color, topLeft = Offset(w*0.1f, h*0.18f), size = Size(w*0.8f, h*0.72f), cornerRadius = CornerRadius(r), style = Stroke(width = 1.5.dp.toPx()))
            drawLine(color, Offset(w*0.1f, h*0.42f), Offset(w*0.9f, h*0.42f), strokeWidth = 1.dp.toPx())
            drawLine(color,
                Offset(w*0.33f, h*0.1f),
                Offset(w*0.33f, h*0.28f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w*0.67f, h*0.1f),
                Offset(w*0.67f, h*0.28f), strokeWidth = 1.5.dp.toPx(), cap = StrokeCap.Round)
            drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(w*0.35f, h*0.62f))
            drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(w*0.55f, h*0.62f))
            drawCircle(color, radius = 1.8.dp.toPx(), center = Offset(w*0.75f, h*0.62f))
        }
        is NavIcon.Homework -> Canvas(modifier = size) {
            val w = this.size.width; val h = this.size.height
            drawRoundRect(color, topLeft = Offset(w*0.15f, h*0.08f), size = Size(w*0.7f, h*0.84f), cornerRadius = CornerRadius(2.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
            drawLine(color,
                Offset(w*0.3f, h*0.38f),
                Offset(w*0.7f, h*0.38f), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w*0.3f, h*0.52f),
                Offset(w*0.7f, h*0.52f), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
            drawLine(color,
                Offset(w*0.3f, h*0.66f),
                Offset(w*0.55f, h*0.66f), strokeWidth = 1.dp.toPx(), cap = StrokeCap.Round)
        }
        is NavIcon.Profile -> Canvas(modifier = size) {
            val w = this.size.width; val h = this.size.height
            drawCircle(color, radius = w*0.22f, center = Offset(w*0.5f, h*0.32f), style = Stroke(width = 1.5.dp.toPx()))
            val path = Path().apply {
                moveTo(w*0.12f, h*0.88f)
                cubicTo(w*0.12f, h*0.65f, w*0.88f, h*0.65f, w*0.88f, h*0.88f)
            }
            drawPath(path, color, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
        }
        else -> Canvas(modifier = size) {
            val w = this.size.width; val h = this.size.height
            drawRoundRect(color, topLeft = Offset(w*0.1f, h*0.1f), size = Size(w*0.8f, h*0.8f), cornerRadius = CornerRadius(2.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
        }
    }
}