package org.example.project.data.repository

import org.example.project.core.logging.AppLogger
import org.example.project.core.network.toNetworkError

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import org.example.project.core.network.Result
import org.example.project.core.domain.NetworkError
import org.example.project.data.remote.dto.GroupDto
import org.example.project.data.remote.dto.GroupRequest
import org.example.project.data.remote.dto.LessonDto
import org.example.project.data.remote.dto.LessonRequest
import org.example.project.data.remote.dto.MarkAttendanceRequest
import org.example.project.data.remote.dto.PagedResponse
import org.example.project.data.remote.dto.RoomDto
import org.example.project.data.remote.dto.RoomRequest
import org.example.project.data.remote.dto.ScheduleAllRequest
import org.example.project.data.remote.dto.ScheduleDto
import org.example.project.domain.models.Group
import org.example.project.domain.models.Lesson
import org.example.project.domain.models.Room
import org.example.project.domain.repository.ScheduleRepository


class ScheduleRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings
) : ScheduleRepository {

    private val baseUrl = "api/"
    override suspend fun getLessons(params: LessonRequest): Result<List<Lesson>, NetworkError> {
        return try {
            AppLogger.d("ScheduleRepository", "LESSONS REQUEST body: $params")

            val response = httpClient.post("api/Lesson/by-schedule") {
                contentType(ContentType.Application.Json)
                setBody(params)
            }

            val bodyText = response.bodyAsText()
            AppLogger.d("ScheduleRepository", "DEBUG: Статус: ${response.status}")
            AppLogger.d("ScheduleRepository", "DEBUG: Тело: $bodyText")
            AppLogger.d("ScheduleRepository", "DEBUG: Params: $params")

            if (response.status.value in 200..299) {
                val json = Json { ignoreUnknownKeys = true }
                val dtos = json.decodeFromString<PagedResponse<LessonDto>>(bodyText).items
                Result.Success(dtos.map { it.toDomain() })
            }else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("ScheduleRepository", "LESSONS EXCEPTION: ${e::class.simpleName} — ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
    override suspend fun getScheduleIds(orgId: String): Result<List<String>, NetworkError> {
        return try {
            val response = httpClient.post("api/Schedule/get-all-schedules") {
                contentType(ContentType.Application.Json)
                setBody(ScheduleAllRequest(organizationId = orgId, pageNumber = 1, pageSize = 100, includeDeleted = false))
            }
            val body = response.bodyAsText()
            AppLogger.d("ScheduleRepository", "SCHEDULE IDS STATUS: ${response.status}")
            AppLogger.d("ScheduleRepository", "SCHEDULE IDS BODY: ${body.take(200)}")
            if (response.status == HttpStatusCode.OK) {
                val paged = Json.decodeFromString<PagedResponse<ScheduleDto>>(body)
                Result.Success(paged.items.map { it.id })
            } else Result.Error(response.status.toNetworkError())
        } catch (e: Exception) {
            AppLogger.d("ScheduleRepository", "SCHEDULE IDS EXCEPTION: ${e::class.simpleName} — ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
    override suspend fun getGroups(orgId: String): Result<List<Group>, NetworkError> {
        return try {
            val response = httpClient.post("${baseUrl}Group/get-groups") {
                contentType(ContentType.Application.Json)
                setBody(GroupRequest(organizationId = orgId))
            }

            if (response.status == HttpStatusCode.OK) {

                val dtos = response.body<PagedResponse<GroupDto>>().items
                Result.Success(dtos.map { Group(it.id, it.name) })
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun getRooms(orgId: String): Result<List<Room>, NetworkError> {
        return try {
            val response = httpClient.post("${baseUrl}Room/GetAllRooms") {
                contentType(ContentType.Application.Json)
                setBody(RoomRequest(organizationId = orgId))
            }

            if (response.status == HttpStatusCode.OK) {
                val dtos = response.body<PagedResponse<RoomDto>>().items
                Result.Success(dtos.map { Room(it.id, it.name) })
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }
    override suspend fun getLessonById(lessonId: String): Result<LessonDto, NetworkError> {
        return try {
            val response = httpClient.get("api/Lesson/GetLessonById/$lessonId")
            if (response.status.value in 200..299)
                Result.Success(response.body<LessonDto>())
            else Result.Error(response.status.toNetworkError())
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun markAttendance(request: MarkAttendanceRequest): Result<Unit, NetworkError> {
        return try {
            val response = httpClient.post("api/Attendance/mark-bulk") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.value in 200..299) Result.Success(Unit)
            else Result.Error(response.status.toNetworkError())
        } catch (e: Exception) {
            Result.Error(e.toNetworkError())
        }
    }
}


fun LessonDto.toDomain(): Lesson = Lesson(
    id = id,
    subjectName = subject?.subjectName ?: "—",
    teacherName = teacher?.name ?: "—",
    roomName = room?.name ?: "—",
    startTime = startTime.substringBeforeLast(":"),
    endTime = endTime.substringBeforeLast(":"),
    groupName = group?.name ?: "—",
    date = date ?: ""
)