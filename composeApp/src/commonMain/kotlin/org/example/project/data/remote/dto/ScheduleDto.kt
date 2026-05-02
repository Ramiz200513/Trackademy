package org.example.project.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LessonRequest(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val organizationId: String,
    val fromDate: String,
    val toDate: String,
    val scheduleId: String? = null,
    val groupId: String? = null,
    val teacherId: String? = null,
    val roomId: String? = null,
    val subjectId: String? = null,
    val groupIds: List<String>? = null
)

@Serializable
data class LessonDto(
    val id: String,
    val date: String? = null,
    val startTime: String,
    val endTime: String,
    val lessonStatus: String? = null,
    val subject: SubjectInfo? = null,
    val teacher: TeacherInfo? = null,
    val room: RoomInfo? = null,
    val group: GroupInfo? = null,
    val cancelReason: String? = null,
    val note: String? = null,
    val isMakeUp: Boolean = false,
    val students: List<StudentInLessonDto> = emptyList()
)

@Serializable
data class StudentInLessonDto(
    val id: String,
    val fullName: String,
    val hasPhoto: Boolean = false,
    val attendanceStatus: Int? = null,
    val grade: Int? = null,
    val comment: String? = null,
    val isFrozen: Boolean = false,
    val remainingLessons: Int = 0
)
@Serializable
data class SubjectInfo(
    val subjectId: String,
    val subjectName: String
)

@Serializable
data class TeacherInfo(
    val id: String,
    val name: String
)

@Serializable
data class RoomInfo(
    val id: String,
    val name: String
)

@Serializable
data class GroupInfo(
    val id: String,
    val name: String
)

@Serializable
data class GroupDto(val id: String, val name: String)

@Serializable
data class RoomDto(val id: String, val name: String)

@Serializable
data class SubjectDto(val id: String, val name: String)


@Serializable
data class GroupRequest(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val organizationId: String,
    val subjectId: String? = null,
    val teacherId: String? = null,
    val search: String? = null
)

@Serializable
data class RoomRequest(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val organizationId: String
)

@Serializable
data class SubjectRequest(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val organizationId: String
)

@Serializable
data class UserRequest(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val search: String? = null,
    val groupIds: List<String>? = null,
    val roleIds: List<Int>? = null,
    val isTrial: Boolean? = null,
    val isDeleted: Boolean? = null,
    val organizationId: String,
    val sortBy: String? = null,
    val sortOrder: String? = null
)

@Serializable
data class ScheduleAllRequest(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val organizationId: String,
    val groupId: String? = null,
    val teacherId: String? = null,
    val roomId: String? = null,
    val subjectId: String? = null,
    val includeDeleted: Boolean = true
)

@Serializable
data class ScheduleDto(
    val id: String,
    val name: String? = null
)
@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val totalCount: Int,
    val pageNumber: Int,
    val pageSize: Int
)