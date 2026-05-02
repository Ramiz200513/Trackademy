package org.example.project.feature.student.assignment

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.ui.components.DownloadSnackbarState
import org.example.project.domain.models.Assignment
import org.example.project.domain.models.AssignmentGroup
import org.example.project.domain.repository.AssignmentRepository
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.AssignmentDetailDto
import org.example.project.data.remote.dto.StudentSubmissionDto
import org.example.project.data.remote.dto.SubmissionDetailFullDto
import org.example.project.domain.utils.FileSaver

class AssignmentComponent(
    componentContext: ComponentContext,
    private val repository: AssignmentRepository,
    private val fileSaver: FileSaver
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val _state = MutableStateFlow(AssignmentState())
    val state: StateFlow<AssignmentState> = _state.asStateFlow()

    private var lastDownloadedBytes: ByteArray? = null
    private var lastDownloadedName: String? = null

    init { loadAssignments() }
    fun downloadAndSaveFile(url: String, fileName: String) {
        scope.launch {
            _state.update {
                it.copy(
                    isDownloadingFile = true,
                    downloadSnackbar = DownloadSnackbarState.InProgress
                )
            }

            when (val result = repository.downloadFile(url)) {
                is Result.Success -> {
                    // Сохраняем байты для повторного открытия
                    lastDownloadedBytes = result.data
                    lastDownloadedName = fileName

                    // Сразу сохраняем на диск
                    fileSaver.saveFile(result.data, fileName)

                    _state.update {
                        it.copy(
                            isDownloadingFile = false,
                            downloadSnackbar = DownloadSnackbarState.Success(fileName)
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isDownloadingFile = false,
                            downloadSnackbar = DownloadSnackbarState.Error("Не удалось скачать файл")
                        )
                    }
                }
            }
        }
    }

    /** Вызывается при нажатии кнопки "Открыть" в снекбаре */
    fun openDownloadedFile() {
        val bytes = lastDownloadedBytes ?: return
        val name = lastDownloadedName ?: return
        fileSaver.saveFile(bytes, name)
    }

    /** Скрыть снекбар */
    fun dismissSnackbar() {
        _state.update { it.copy(downloadSnackbar = DownloadSnackbarState.Hidden) }
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
                                            submission = SubmissionDetailFullDto(
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
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = "Не удалось загрузить детали")
                }
            }
        }
    }

    fun submitWork(assignmentId: String, textContent: String, fileBytes: ByteArray?) {
        closeDetail()
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            when (repository.submitAssignment(assignmentId, textContent, fileBytes)) {
                is Result.Success -> loadAssignments()
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = "Ошибка при отправке работы")
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
                is Result.Success -> _state.update {
                    it.copy(isLoading = false, groups = result.data)
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = "Ошибка загрузки")
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class AssignmentState(
    val isLoading: Boolean = false,
    val isDownloadingFile: Boolean = false,
    val groups: List<AssignmentGroup> = emptyList(),
    val selectedAssignment: Assignment? = null,
    val error: String? = null,
    val selectedDetails: AssignmentDetailDto? = null,
    val downloadSnackbar: DownloadSnackbarState = DownloadSnackbarState.Hidden
)