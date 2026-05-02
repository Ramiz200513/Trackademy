package org.example.project.data.remote.dto
import kotlinx.serialization.Serializable

@Serializable
data class MyAssignmentsResponse(
    val pending: List<AssignmentItemDto> = emptyList(),
    val submitted: List<AssignmentItemDto> = emptyList(),
    val graded: List<AssignmentItemDto> = emptyList(),
    val overdue: List<AssignmentItemDto> = emptyList()
)
@Serializable
data class AssignmentDto(
    val id: String,
    val description: String,
    val groupId: String,
    val assignedDate: String,
    val dueDate: String,
    val hasAttachment: Boolean = false,
    val attachmentName: String? = null,
    val attachmentSize: Long? = null,
    val group: GroupRefDto? = null,
    val subjectName: String? = null,
    val studentSubmissions: List<StudentSubmissionDto> = emptyList()
)

@Serializable
data class GroupRefDto(
    val id: String,
    val name: String
)

@Serializable
data class StudentSubmissionDto(
    val studentId: String,
    val studentName: String,
    val studentLogin: String,
    val submission: SubmissionDetailFullDto? = null
)

@Serializable
data class SubmissionDto(
    val id: String? = null,
    val submittedAt: String? = null,
    val grade: Int? = null,
    val comment: String? = null,
    val hasAttachment: Boolean = false
)

@Serializable
data class AttendanceFilterRequest(
    val groupId: String? = null,
    val studentId: String? = null,
    val status: Int? = null,
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val pageNumber: Int = 1,
    val pageSize: Int = 20
)

@Serializable
data class AssignmentItemDto(
    val assignmentId: String,
    val description: String,
    val assignedDate: String? = null,
    val dueDate: String? = null,
    val submissionId: String? = null,
    val status: Int? = null,
    val score: Int? = null,
    val group: AssignmentGroupDto? = null,
    val subject: AssignmentSubjectDto? = null
)

@Serializable
data class AssignmentGroupDto(val id: String, val name: String)

@Serializable
data class AssignmentSubjectDto(val id: String, val name: String)
@Serializable
data class AssignmentDetailDto(
    val id: String,
    val description: String,
    val attachmentName: String? = null,
    val attachmentSize: Long? = null,
    val hasAttachment: Boolean = false,
    val studentSubmissions: List<StudentSubmissionDto> = emptyList()
)
@Serializable
data class SubmissionDetailDto(
    val id: String,
    val textContent: String? = null,
    val files: List<SubmissionFileDto> = emptyList()
)

@Serializable
data class SubmissionFileDto(
    val id: String,
    val originalFileName: String,
    val downloadUrl: String
)
@Serializable
data class FileDto(
    val id: String,
    val originalFileName: String,
    val contentType: String,
    val fileSize: Long,
    val uploadedAt: String? = null,
    val isImage: Boolean,
    val downloadUrl: String
)
@Serializable
data class AssignmentFilterRequest(
    val groupId: String? = null,
    val organizationId: String? = null,  // ← добавить
    val pageNumber: Int = 1,
    val pageSize: Int = 20
)
@Serializable
data class SubmissionResponseDto(
    val id: String,
    val assignmentId: String,
    val textContent: String?,
    val status: Int,
    val files: List<FileDto> = emptyList(),
)
@Serializable
data class CreateSubmissionRequest(
    val textContent: String
)
@Serializable
data class SubmissionDetailFullDto(
    val id: String,
    val assignmentId: String,
    val textContent: String? = null,
    val status: Int? = null,
    val statusName: String? = null,
    val score: Int? = null,
    val submittedAt: String? = null,
    val files: List<FileDto> = emptyList()
)