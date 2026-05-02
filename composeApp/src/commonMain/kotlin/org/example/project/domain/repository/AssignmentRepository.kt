package org.example.project.domain.repository

import org.example.project.core.domain.NetworkError
import org.example.project.domain.models.AssignmentGroup
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.AssignmentDetailDto
import org.example.project.data.remote.dto.SubmissionDetailFullDto
import org.example.project.domain.models.Assignment

interface AssignmentRepository {
    suspend fun getMyAssignments(): Result<List<AssignmentGroup>, NetworkError>

    suspend fun getAssignmentDetails(id: String): Result<AssignmentDetailDto, NetworkError>

    suspend fun submitAssignment(assignmentId: String, textContent: String,fileBytes: ByteArray? = null): Result<Unit, NetworkError>
    suspend fun downloadFile(url:String): Result<ByteArray, NetworkError>
    suspend fun getSubmissionDetails(submissionId: String): Result<SubmissionDetailFullDto, NetworkError>
}


