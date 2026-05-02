package org.example.project.feature.teacher

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.remote.dto.AssignmentDto
import org.example.project.data.remote.dto.AssignmentFilterRequest
import org.example.project.data.remote.dto.GroupDto
import org.example.project.domain.repository.TeacherRepository
import org.example.project.core.network.Result
class TeacherAssignmentComponent(
    componentContext: ComponentContext,
    private val repository: TeacherRepository,
    private val orgId: String,
    val onLogout: () -> Unit,
    private val settings: Settings
) : ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow(TeacherAssignmentState())
    val state: StateFlow<TeacherAssignmentState> = _state.asStateFlow()

    init {
        val fullName = settings.getString("fullName", "Преподаватель")
        val initials = fullName.split(" ").filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }.take(2).joinToString("")

        _state.update { it.copy(userInitials = initials) }
        loadGroups()
        loadAssignments()
    }

    fun loadGroups() {
        scope.launch {
            when (val r = repository.getGroups(orgId)) {
                is Result.Success -> _state.update { it.copy(groups = r.data) }
                is Result.Error -> {}
            }
        }
    }

    fun loadAssignments() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            val filter = AssignmentFilterRequest(
                groupId = _state.value.selectedGroupId,
                organizationId = orgId,
                pageNumber = 1,
                pageSize = 20
            )
            when (val r = repository.getAssignments(filter)) {
                is Result.Success -> _state.update { it.copy(
                    assignments = r.data.items,
                    isLoading = false
                )}
                is Result.Error -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectGroup(groupId: String?) {
        _state.update { it.copy(selectedGroupId = groupId) }
        loadAssignments()
    }

    fun openDetail(assignment: AssignmentDto) {
        scope.launch {
            when (val r = repository.getAssignmentDetail(assignment.id)) {
                is Result.Success -> _state.update { it.copy(selectedAssignment = r.data) }
                is Result.Error -> {}
            }
        }
    }

    fun closeDetail() {
        _state.update { it.copy(selectedAssignment = null) }
    }

    fun openCreate() {
        _state.update { it.copy(showCreateDialog = true) }
    }

    fun closeCreate() {
        _state.update { it.copy(showCreateDialog = false) }
    }

    fun createAssignment(groupId: String, description: String, dueDate: String) {
        scope.launch {
            _state.update { it.copy(isSaving = true) }
            when (repository.createAssignment(groupId, description, dueDate, null, null)) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false, showCreateDialog = false) }
                    loadAssignments()
                }
                is Result.Error -> _state.update { it.copy(isSaving = false) }
            }
        }
    }

    fun deleteAssignment(id: String) {
        scope.launch {
            when (repository.deleteAssignment(id)) {
                is Result.Success -> {
                    _state.update { it.copy(selectedAssignment = null) }
                    loadAssignments()
                }
                is Result.Error -> {}
            }
        }
    }
}

data class TeacherAssignmentState(
    val isLoading: Boolean = false,
    val groups: List<GroupDto> = emptyList(),
    val assignments: List<AssignmentDto> = emptyList(),
    val selectedGroupId: String? = null,
    val selectedAssignment: AssignmentDto? = null,
    val showCreateDialog: Boolean = false,
    val isSaving: Boolean = false,
    val userInitials: String = "??"
)