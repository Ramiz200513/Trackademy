package org.example.project.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class TeacherDashboardDto(
    val totalGroups: Int,
    val ungradedSubmissions: Int,
    val lessonsToday: Int,
    val todaySchedule: List<TodayScheduleDto> = emptyList()
)

@Serializable
data class TodayScheduleDto(
    val lessonId: String,
    val startTime: String,
    val endTime: String,
    val groupName: String,
    val subjectName: String,
    val roomName: String,
    val isPast: Boolean,
    val attendanceRate: Int? = null,
    val presentCount: Int? = null,
    val totalStudents: Int? = null
)
