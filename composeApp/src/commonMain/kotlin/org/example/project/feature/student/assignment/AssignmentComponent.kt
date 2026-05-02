package org.example.project.feature.student.assignment

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.models.Assignment
import org.example.project.domain.models.AssignmentGroup
import org.example.project.domain.repository.AssignmentRepository
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.AssignmentDetailDto
import org.example.project.data.remote.dto.StudentSubmissionDto
import org.example.project.domain.utils.FileSaver

class AssignmentComponent(
    componentContext: ComponentContext,
    private val repository: AssignmentRepository,
    private val fileSaver: FileSaver
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _state = MutableStateFlow(AssignmentState())
    val state: StateFlow<AssignmentState> = _state.asStateFlow()

    init { loadAssignments() }
    fun downloadAndSaveFile(url: String, fileName: String) {
        closeDetail()

        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.downloadFile(url)

            when (result) {
                is Result.Success -> {
                    fileSaver.saveFile(result.data, fileName)
                    _state.update { it.copy(isLoading = false, error = "✅ Файл сохранен: $fileName") }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = "❌ Не удалось скачать файл") }
                }
            }
        }
    }
    fun selectAssignment(assignment: Assignment) {
        _state.update { it.copy(selectedAssignment = assignment, selectedDetails = null, isLoading = true) }

        scope.launch {
            when (val result = repository.getAssignmentDetails(assignment.id)) {
                is Result.Success -> {
                    val details = result.data

                    val submissionId = assignment.submissionId
                    if (submissionId != null) {
                        when (val subResult = repository.getSubmissionDetails(submissionId)) {
                            is Result.Success -> {
                                val detailsWithFiles = details.copy(
                                    studentSubmissions = listOf(
                                        StudentSubmissionDto(
                                            studentId = "me",
                                            studentName = "Моя работа",
                                            studentLogin = "my_login",

                                            submission = org.example.project.data.remote.dto.SubmissionDetailFullDto(
                                                id = subResult.data.id,
                                                assignmentId = assignment.id,
                                                textContent = subResult.data.textContent,
                                                status = subResult.data.status,
                                                files = subResult.data.files
                                            )
                                        )
                                    )
                                )
                                _state.update { it.copy(selectedDetails = detailsWithFiles, isLoading = false) }
                            }
                            is Result.Error -> {
                                _state.update { it.copy(selectedDetails = details, isLoading = false) }
                            }
                        }
                    } else {
                        _state.update { it.copy(selectedDetails = details, isLoading = false) }
                    }
                }
                is Result.Error -> _state.update { it.copy(isLoading = false, error = "Не удалось загрузить детали") }
            }
        }
    }

    fun submitWork(assignmentId: String, textContent: String, fileBytes: ByteArray?) {
        closeDetail()

        scope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = repository.submitAssignment(assignmentId, textContent, fileBytes)

            when (result) {
                is Result.Success -> {
                    loadAssignments()
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = "Ошибка при отправке работы") }
                }
            }
        }
    }
    fun closeDetail() {
        _state.update { it.copy(selectedAssignment = null, selectedDetails = null) }
    }

    private fun loadAssignments() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = repository.getMyAssignments()) {
                is Result.Success<*> -> _state.update {
                    it.copy(isLoading = false, groups = result.data as List<AssignmentGroup>)
                }
                is Result.Error<*> -> _state.update { it.copy(isLoading = false, error = "Ошибка загрузки") }
            }
        }
    }
    fun clearError() {
        _state.update { it.copy(error = null, isLoading = false) }
    }
}

data class AssignmentState(
    val isLoading: Boolean = false,
    val groups: List<AssignmentGroup> = emptyList(),
    val selectedAssignment: Assignment? = null,
    val error: String? = null,
    val selectedDetails: AssignmentDetailDto? = null
)