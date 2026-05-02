package org.example.project.data.repository

import org.example.project.core.network.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import org.example.project.core.domain.NetworkError
import com.russhwolf.settings.Settings
import io.ktor.client.request.delete

import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.contentType
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.example.project.data.remote.dto.AssignmentDto
import org.example.project.data.remote.dto.AssignmentFilterRequest
import org.example.project.data.remote.dto.AttendanceFilterRequest
import org.example.project.data.remote.dto.AttendanceGroupDetailsDto
import org.example.project.data.remote.dto.AttendanceGroupStatsDto
import org.example.project.data.remote.dto.AttendanceRecordDto
import org.example.project.data.remote.dto.GroupDto
import org.example.project.data.remote.dto.LessonDto
import org.example.project.data.remote.dto.LessonRequest
import org.example.project.data.remote.dto.MarkAttendanceRequest
import org.example.project.data.remote.dto.PagedResponse
import org.example.project.data.remote.dto.TeacherDashboardDto
import org.example.project.domain.repository.TeacherRepository

class TeacherRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : TeacherRepository {
    override suspend fun getLessonsBySchedule(request: LessonRequest): Result<PagedResponse<LessonDto>, NetworkError> {
        return try {
            val resp = httpClient.post("api/Lesson/by-schedule") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (resp.status.value in 200..299) Result.Success(resp.body<PagedResponse<LessonDto>>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun getLessonById(lessonId: String): Result<LessonDto, NetworkError> {
        return try {
            val resp = httpClient.get("api/Lesson/GetLessonById/$lessonId") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
            }
            if (resp.status.value in 200..299) Result.Success(resp.body<LessonDto>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }
    private fun token() = settings.getString("token", "")
    override suspend fun getTeacherDashboard(): Result<TeacherDashboardDto, NetworkError> {
        return try {
            val resp = httpClient.get("api/Dashboard/teacher") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
            }
            if (resp.status.value in 200..299) {
                Result.Success(resp.body<TeacherDashboardDto>())
            } else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }
    override suspend fun getGroups(orgId: String): Result<List<GroupDto>, NetworkError> {
        return try {
            val resp = httpClient.post("api/Group/get-groups") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("organizationId", orgId)
                    put("pageSize", 1000)
                    put("pageNumber", 1)
                })
            }
            if (resp.status.value in 200..299)
                Result.Success(resp.body<PagedResponse<GroupDto>>().items)
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun getAttendances(filter: AttendanceFilterRequest): Result<PagedResponse<AttendanceRecordDto>, NetworkError> {
        return try {
            val resp = httpClient.post("api/Attendance/get-all-attendances") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                contentType(ContentType.Application.Json)
                setBody(filter)
            }
            if (resp.status.value in 200..299)
                Result.Success(resp.body<PagedResponse<AttendanceRecordDto>>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun getGroupAttendanceStats(orgId: String): Result<PagedResponse<AttendanceGroupStatsDto>, NetworkError> {
        return try {
            val resp = httpClient.post("api/Attendance/stats/groups") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("organizationId", orgId)
                    put("pageSize", 100)
                    put("pageNumber", 1)
                })
            }
            if (resp.status.value in 200..299)
                Result.Success(resp.body<PagedResponse<AttendanceGroupStatsDto>>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun getGroupAttendanceDetails(groupId: String): Result<AttendanceGroupDetailsDto, NetworkError> {
        return try {
            val resp = httpClient.get("api/Attendance/stats/groups/$groupId/details") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
            }
            if (resp.status.value in 200..299)
                Result.Success(resp.body<AttendanceGroupDetailsDto>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun markAttendance(request: MarkAttendanceRequest): Result<Unit, NetworkError> {
        return try {
            val resp = httpClient.post("api/Attendance/mark-bulk") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (resp.status.value in 200..299) Result.Success(Unit)
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun getAssignments(filter: AssignmentFilterRequest): Result<PagedResponse<AssignmentDto>, NetworkError> {
        return try {
            val resp = httpClient.post("api/Assignment/get-assignments") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                contentType(ContentType.Application.Json)
                setBody(filter)
            }
            if (resp.status.value in 200..299)
                Result.Success(resp.body<PagedResponse<AssignmentDto>>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun getAssignmentDetail(id: String): Result<AssignmentDto, NetworkError> {
        return try {
            val resp = httpClient.get("api/Assignment/$id") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
            }
            if (resp.status.value in 200..299)
                Result.Success(resp.body<AssignmentDto>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun deleteAssignment(id: String): Result<Unit, NetworkError> {
        return try {
            val resp = httpClient.delete("api/Assignment/$id") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
            }
            if (resp.status.value in 200..299) Result.Success(Unit)
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun createAssignment(
        groupId: String, description: String, dueDate: String,
        fileBytes: ByteArray?, fileName: String?
    ): Result<String, NetworkError> {
        return try {
            val resp = httpClient.post("api/Assignment/create-with-file") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                setBody(MultiPartFormDataContent(formData {
                    append("groupId", groupId)
                    append("description", description)
                    append("dueDate", dueDate)
                    if (fileBytes != null && fileName != null) {
                        append("file", fileBytes, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=$fileName")
                        })
                    }
                }))
            }
            if (resp.status.value in 200..299)
                Result.Success(resp.body<String>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }

    override suspend fun updateAssignment(id: String, description: String, dueDate: String): Result<Unit, NetworkError> {
        return try {
            val resp = httpClient.put("api/Assignment/$id") {
                header(HttpHeaders.Authorization, "Bearer ${token()}")
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("description", description)
                    put("dueDate", dueDate)
                })
            }
            if (resp.status.value in 200..299) Result.Success(Unit)
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) { Result.Error(NetworkError.Unknown) }
    }
}