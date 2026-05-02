package org.example.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DashboardDto(
    val totalGroups: Int,
    val ungradedSubmissions: Int,
    val lessonsToday: Int,
    val todaySchedule: List<TodayLessonDto>
)

@Serializable
data class TodayLessonDto(
    val lessonId: String,
    val startTime: String,
    val endTime: String,
    val groupName: String,
    val subjectName: String,
    val roomName: String,
    val isPast: Boolean,
    val attendanceRate: Double? = null,
    val presentCount: Int? = null,
    val totalStudents: Int? = null
)
@Serializable
data class PerformanceAssignmentDto(
    val assignmentId: String,
    val description: String,
    val subjectName: String,
    val dueDate: String,
    val status: String,
    val isOverdue: Boolean
)

@Serializable
data class PerformanceDto(
    val averageGrade: Double? = 0.0,
    val attendanceRate: Double? = 0.0,
    val activeAssignments: Int? = 0,
    val activeAssignmentsList: List<PerformanceAssignmentDto> = emptyList(),
    val recentGrades: List<GradeDto> = emptyList()
)

@Serializable
data class GradeDto(
    val subjectName: String,
    val grade: Int,
    val gradedAt: String
)