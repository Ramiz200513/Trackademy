package org.example.project.data.repository

import org.example.project.core.logging.AppLogger
import org.example.project.core.network.toNetworkError

import org.example.project.core.network.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import org.example.project.data.remote.dto.AssignmentItemDto
import org.example.project.data.remote.dto.MyAssignmentsResponse
import org.example.project.domain.models.Assignment
import org.example.project.domain.models.AssignmentGroup
import org.example.project.domain.repository.AssignmentRepository
import org.example.project.core.domain.NetworkError
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.client.request.forms.append
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import org.example.project.data.remote.dto.AssignmentDetailDto
import org.example.project.data.remote.dto.SubmissionResponseDto
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import org.example.project.data.remote.dto.SubmissionDetailFullDto

class AssignmentRepositoryImpl(
    private val httpClient: HttpClient,
    private val settings: Settings,
) : AssignmentRepository {

    override suspend fun getAssignmentDetails(id: String): Result<AssignmentDetailDto, NetworkError> {
        return try {
            val response = httpClient.get("api/Assignment/$id")
            AppLogger.d("AssignmentRepository", "📋 DETAILS RAW: ${response.bodyAsText()}")

            if (response.status.value in 200..299) {
                Result.Success(response.body<AssignmentDetailDto>())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("AssignmentRepository", "DETAILS ERROR: ${e::class.simpleName} — ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun submitAssignment(
        assignmentId: String,
        textContent: String,
        fileBytes: ByteArray?
    ): Result<Unit, NetworkError> {
        return try {

            val draftResponse = httpClient.post("api/Submission/assignment/$assignmentId") {

                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("textContent", textContent)

                            if (fileBytes != null) {
                                append(
                                    "files",
                                    fileBytes,
                                    Headers.build {
                                        append(HttpHeaders.ContentType, "image/jpeg")
                                        append(HttpHeaders.ContentDisposition, "filename=\"homework_photo.jpg\"")
                                    }
                                )
                            }
                        }
                    )
                )
            }

            if (draftResponse.status.value in 200..299) {
                val submission = draftResponse.body<SubmissionResponseDto>()
                val submissionId = submission.id

                val submitResponse = httpClient.post("api/Submission/$submissionId/submit")

                if (submitResponse.status.value in 200..299) {
                    Result.Success(Unit)
                } else {
                    Result.Error(submitResponse.status.toNetworkError())
                }
            } else {
                Result.Error(draftResponse.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("AssignmentRepository", "SUBMIT ERROR: ${e::class.simpleName} — ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }

    override suspend fun downloadFile(url: String): Result<ByteArray, NetworkError> {
        return try {

            val path = when {
                url.startsWith("http") -> url
                url.startsWith("api/") -> url
                else -> "api/$url"
            }

            AppLogger.d("AssignmentRepository", "🌐 FINAL PATH: $path")

            val response = httpClient.get(path)

            AppLogger.d("AssignmentRepository", "📡 STATUS: ${response.status.value}")

            if (response.status.value in 200..299) {
                Result.Success(response.readBytes())
            } else {
                AppLogger.d("AssignmentRepository", "❌ ERROR BODY: ${response.bodyAsText()}")
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("AssignmentRepository", "❌ EXCEPTION: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
    override suspend fun getSubmissionDetails(submissionId: String): Result<SubmissionDetailFullDto, NetworkError> {
        return try {
            val response = httpClient.get("api/Submission/$submissionId")
            AppLogger.d("AssignmentRepository", "📋 SUBMISSION RAW: ${response.bodyAsText()}")
            if (response.status.value in 200..299) {
                Result.Success(response.body<SubmissionDetailFullDto>())
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("AssignmentRepository", "SUBMISSION ERROR: ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
    override suspend fun getMyAssignments(): Result<List<AssignmentGroup>, NetworkError> {
        return try {
            val orgId = settings.getString("orgId", "")

            val response = httpClient.post("api/Assignment/my-assignments") {
                val jsonPayload = """{"organizationId":"$orgId"}"""
                setBody(TextContent(jsonPayload, ContentType.Application.Json))
            }

            if (response.status.value in 200..299) {
                val dto = response.body<MyAssignmentsResponse>()
                val groups = listOf(
                    AssignmentGroup("Просрочено", dto.overdue.map { it.toDomain(isOverdue = true) }),
                    AssignmentGroup("К выполнению", dto.pending.map { it.toDomain() }),
                    AssignmentGroup("На проверке", dto.submitted.map { it.toDomain() }),
                    AssignmentGroup("Проверено", dto.graded.map { it.toDomain() })
                ).filter { it.assignments.isNotEmpty() }

                Result.Success(groups)
            } else {
                Result.Error(response.status.toNetworkError())
            }
        } catch (e: Exception) {
            AppLogger.d("AssignmentRepository", "ASSIGNMENT ERROR: ${e::class.simpleName} — ${e.message}")
            Result.Error(e.toNetworkError())
        }
    }
}

fun AssignmentItemDto.toDomain(isOverdue: Boolean = false) = Assignment(
    id = assignmentId,
    description = description,
    subjectName = subject?.name ?: "—",
    groupName = group?.name ?: "—",
    dueDate = dueDate?.take(10) ?: "—",
    assignedDate = assignedDate?.take(10) ?: "—",
    status = when {
        isOverdue -> "Просрочено"
        status == 2 -> "Проверено"
        status == 1 -> "На проверке"
        status == 0 -> "К выполнению"
        else -> "Не начато"
    },
    isOverdue = isOverdue,
    submissionId = submissionId,
    score = score
)