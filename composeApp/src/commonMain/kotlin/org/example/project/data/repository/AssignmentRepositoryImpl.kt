package org.example.project.data.repository

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
            val token = settings.getString("token", "")
            val response = httpClient.get("api/Assignment/$id") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            println("📋 DETAILS RAW: ${response.bodyAsText()}")

            if (response.status.value in 200..299) {
                Result.Success(response.body<AssignmentDetailDto>())
            } else {
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            println("DETAILS ERROR: ${e::class.simpleName} — ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }

    override suspend fun submitAssignment(
        assignmentId: String,
        textContent: String,
        fileBytes: ByteArray?
    ): Result<Unit, NetworkError> {
        return try {
            val token = settings.getString("token", "")

            val draftResponse = httpClient.post("api/Submission/assignment/$assignmentId") {
                header(HttpHeaders.Authorization, "Bearer $token")

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

                val submitResponse = httpClient.post("api/Submission/$submissionId/submit") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }

                if (submitResponse.status.value in 200..299) {
                    Result.Success(Unit)
                } else {
                    Result.Error(NetworkError.ServerError)
                }
            } else {
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            println("SUBMIT ERROR: ${e::class.simpleName} — ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }

    override suspend fun downloadFile(url: String): Result<ByteArray, NetworkError> {
        return try {
            val token = settings.getString("token", "")

            val path = when {
                url.startsWith("http") -> url
                url.startsWith("api/") -> url
                else -> "api/$url"
            }

            println("🌐 FINAL PATH: $path")

            val response = httpClient.get(path) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            println("📡 STATUS: ${response.status.value}")

            if (response.status.value in 200..299) {
                Result.Success(response.readBytes())
            } else {
                println("❌ ERROR BODY: ${response.bodyAsText()}")
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            println("❌ EXCEPTION: ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }
    override suspend fun getSubmissionDetails(submissionId: String): Result<SubmissionDetailFullDto, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val response = httpClient.get("api/Submission/$submissionId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            println("📋 SUBMISSION RAW: ${response.bodyAsText()}")
            if (response.status.value in 200..299) {
                Result.Success(response.body<SubmissionDetailFullDto>())
            } else {
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            println("SUBMISSION ERROR: ${e.message}")
            Result.Error(NetworkError.Unknown)
        }
    }
    override suspend fun getMyAssignments(): Result<List<AssignmentGroup>, NetworkError> {
        return try {
            val token = settings.getString("token", "")
            val orgId = settings.getString("orgId", "")

            val response = httpClient.post("api/Assignment/my-assignments") {
                header(HttpHeaders.Authorization, "Bearer $token")
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
                Result.Error(NetworkError.ServerError)
            }
        } catch (e: Exception) {
            println("ASSIGNMENT ERROR: ${e::class.simpleName} — ${e.message}")
            Result.Error(NetworkError.Unknown)
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