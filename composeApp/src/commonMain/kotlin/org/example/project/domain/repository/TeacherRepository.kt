package org.example.project.domain.repository

import org.example.project.core.domain.NetworkError
import org.example.project.data.remote.dto.AssignmentDto
import org.example.project.data.remote.dto.AttendanceFilterRequest
import org.example.project.data.remote.dto.AttendanceGroupDetailsDto
import org.example.project.data.remote.dto.AttendanceGroupStatsDto
import org.example.project.data.remote.dto.AttendanceRecordDto
import org.example.project.data.remote.dto.GroupDto
import org.example.project.data.remote.dto.MarkAttendanceRequest
import org.example.project.data.remote.dto.PagedResponse
import org.example.project.data.remote.dto.AssignmentFilterRequest
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.LessonDto
import org.example.project.data.remote.dto.LessonRequest
import org.example.project.data.remote.dto.TeacherDashboardDto

interface TeacherRepository {
    suspend fun getGroups(orgId: String): Result<List<GroupDto>, NetworkError>
    suspend fun getLessonsBySchedule(request: LessonRequest): Result<PagedResponse<LessonDto>, NetworkError>
    suspend fun getLessonById(lessonId: String): Result<LessonDto, NetworkError>
    suspend fun getAttendances(filter: AttendanceFilterRequest): Result<PagedResponse<AttendanceRecordDto>, NetworkError>
    suspend fun getTeacherDashboard(): Result<TeacherDashboardDto, NetworkError>
    suspend fun getGroupAttendanceStats(orgId: String): Result<PagedResponse<AttendanceGroupStatsDto>, NetworkError>

    suspend fun getGroupAttendanceDetails(groupId: String): Result<AttendanceGroupDetailsDto, NetworkError>

    suspend fun markAttendance(request: MarkAttendanceRequest): Result<Unit, NetworkError>

    suspend fun getAssignments(filter: AssignmentFilterRequest): Result<PagedResponse<AssignmentDto>, NetworkError>

    suspend fun getAssignmentDetail(id: String): Result<AssignmentDto, NetworkError>

    suspend fun deleteAssignment(id: String): Result<Unit, NetworkError>

    suspend fun createAssignment(
        groupId: String, description: String, dueDate: String,
        fileBytes: ByteArray?, fileName: String?
    ): Result<String, NetworkError>

    suspend fun updateAssignment(id: String, description: String, dueDate: String): Result<Unit, NetworkError>
}