package org.example.project.feature.schedule

import org.example.project.domain.models.Lesson
import kotlinx.datetime.LocalDate
import org.example.project.data.remote.dto.AttendanceMarkDto
import org.example.project.data.remote.dto.LessonDto

enum class ViewMode { DAY, WEEK, MONTH, LIST }

data class ScheduleState(
    val isLoading: Boolean = false,
    val selectedDate: LocalDate,
    val weekStart: LocalDate,
    val lessons: List<Lesson> = emptyList(),
    val error: String? = null,
    val calendarDays: List<LocalDate> = emptyList(),
    val viewMode: ViewMode = ViewMode.WEEK,
    // новое:
    val selectedLesson: LessonDto? = null,
    val isLessonLoading: Boolean = false,
    val isSavingAttendance: Boolean = false
)

sealed interface ScheduleIntent {
    data class SelectDate(val date: LocalDate) : ScheduleIntent
    data object Refresh : ScheduleIntent
    data object NextWeek : ScheduleIntent
    data object PrevWeek : ScheduleIntent
    data class SetViewMode(val mode: ViewMode) : ScheduleIntent
    data object PrevMonth : ScheduleIntent
    data object NextMonth : ScheduleIntent
    data class OpenLesson(val lessonId: String) : ScheduleIntent
    data object CloseLesson : ScheduleIntent
    data class SaveAttendance(val attendances: List<AttendanceMarkDto>) : ScheduleIntent
}