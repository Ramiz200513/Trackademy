package org.example.project.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceRecordDto(
    val id: String,
    val studentId: String,
    val studentName: String,
    val studentLogin: String,
    val groupId: String,
    val lessonId: String,
    val date: String,
    val status: Int,
    val statusName: String,
    val isPaid: Boolean = false,
    val grade: Int? = null,
    val comment: String? = null,
    val subjectName: String,
    val groupName: String,
    val teacherName: String,
    val startTime: String,
    val endTime: String
)

@Serializable
data class AttendanceGroupStatsDto(
    val groupId: String,
    val groupName: String,
    val groupCode: String,
    val subjectName: String,
    val totalStudents: Int,
    val totalLessons: Int,
    val attendedCount: Int,
    val missedCount: Int,
    val attendanceRate: Double,
    val previousPeriodRate: Double,
    val trend: Double
)

@Serializable
data class AttendanceGroupDetailsDto(
    val groupnId: String,
    val groupName: String,
    val subjectName: String,
    val students: List<StudentAttendanceStatsDto>
)

@Serializable
data class StudentAttendanceStatsDto(
    val studentId: String,
    val studentName: String,
    val studentLogin: String,
    val totalLessons: Int,
    val attendedLessons: Int,
    val lateLessons: Int,
    val missedLessons: Int,
    val attendanceRate: Double
)

@Serializable
data class MarkAttendanceRequest(
    val lessonId: String,
    val attendances: List<AttendanceMarkDto>
)

@Serializable
data class AttendanceMarkDto(
    val studentId: String,
    val status: Int,
    val grade: Int? = null,
    val comment: String? = null
)