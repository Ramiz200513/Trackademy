package org.example.project.data.repository

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
            val token = settings.getString("token", "")
            println("LESSONS REQUEST body: $params")
            println("LESSONS TOKEN: ${token.take(20)}")

            val response = httpClient.post("api/Lesson/by-schedule") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(params)
            }

            val bodyText = response.bodyAsText()
            println("DEBUG: Статус: ${response.status}")
            println("DEBUG: Тело: $bodyText")
            println("DEBUG: Params: $params")

            if (response.status.value in 200..299) {
                val json = Json { ignoreUnknownKeys = true }
                val dtos = json.decodeFromString<PagedResponse<LessonDto>>(bodyText).items
                Result.Success(dtos.map { it.toDomain() })
            }else {
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            println("LESSONS EXCEPTION: ${e::class.simpleName} — ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }
    override suspend fun getScheduleIds(orgId: String): Result<List<String>, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            println("SCHEDULE IDS REQUEST, orgId=$orgId, token=${token.take(10)}")
            val response = httpClient.post("api/Schedule/get-all-schedules") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(ScheduleAllRequest(organizationId = orgId, pageNumber = 1, pageSize = 100, includeDeleted = false))
            }
            val body = response.bodyAsText()
            println("SCHEDULE IDS STATUS: ${response.status}")
            println("SCHEDULE IDS BODY: ${body.take(200)}")
            if (response.status == HttpStatusCode.OK) {
                val paged = Json.decodeFromString<PagedResponse<ScheduleDto>>(body)
                Result.Success(paged.items.map { it.id })
            } else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) {
            println("SCHEDULE IDS EXCEPTION: ${e::class.simpleName} — ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }
    override suspend fun getGroups(orgId: String): Result<List<Group>, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val response = httpClient.post("${baseUrl}Group/get-groups") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(GroupRequest(organizationId = orgId))
            }

            if (response.status == HttpStatusCode.OK) {

                val dtos = response.body<PagedResponse<GroupDto>>().items
                Result.Success(dtos.map { Group(it.id, it.name) })
            } else {
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.Unknown)
        }
    }

    override suspend fun getRooms(orgId: String): Result<List<Room>, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val response = httpClient.post("${baseUrl}Room/GetAllRooms") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(RoomRequest(organizationId = orgId))
            }

            if (response.status == HttpStatusCode.OK) {
                val dtos = response.body<PagedResponse<RoomDto>>().items
                Result.Success(dtos.map { Room(it.id, it.name) })
            } else {
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            Result.Error(NetworkError.Unknown)
        }
    }
    override suspend fun getLessonById(lessonId: String): Result<LessonDto, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val response = httpClient.get("api/Lesson/GetLessonById/$lessonId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            if (response.status.value in 200..299)
                Result.Success(response.body<LessonDto>())
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) {
            Result.Error(NetworkError.Unknown)
        }
    }

    override suspend fun markAttendance(request: MarkAttendanceRequest): Result<Unit, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val response = httpClient.post("api/Attendance/mark-bulk") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.value in 200..299) Result.Success(Unit)
            else Result.Error(NetworkError.ServerError)
        } catch (e: Exception) {
            Result.Error(NetworkError.Unknown)
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