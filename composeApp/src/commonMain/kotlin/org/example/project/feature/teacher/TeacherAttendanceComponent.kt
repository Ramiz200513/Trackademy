package org.example.project.feature.teacher

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.remote.dto.AttendanceFilterRequest
import org.example.project.data.remote.dto.AttendanceGroupDetailsDto
import org.example.project.data.remote.dto.AttendanceGroupStatsDto
import org.example.project.data.remote.dto.AttendanceRecordDto
import org.example.project.data.remote.dto.GroupDto
import org.example.project.domain.repository.TeacherRepository
import org.example.project.core.network.Result

class TeacherAttendanceComponent(
    componentContext: ComponentContext,
    private val repository: TeacherRepository,
    private val orgId: String,
    val onLogout: () -> Unit
) : ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow(TeacherAttendanceState())
    val state: StateFlow<TeacherAttendanceState> = _state.asStateFlow()

    init {
        loadGroups()
        loadStats()
        loadAttendances()
    }

    fun loadGroups() {
        scope.launch {
            when (val r = repository.getGroups(orgId)) {
                is Result.Success -> _state.update { it.copy(groups = r.data) }
                is Result.Error -> {}
            }
        }
    }

    fun loadStats() {
        scope.launch {
            when (val r = repository.getGroupAttendanceStats(orgId)) {
                is Result.Success -> _state.update { it.copy(groupStats = r.data.items) }
                is Result.Error -> {}
            }
        }
    }

    fun loadAttendances() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            val filter = AttendanceFilterRequest(
                groupId = _state.value.selectedGroupId,
                status = _state.value.selectedStatus,
                pageNumber = 1,
                pageSize = 20
            )
            when (val r = repository.getAttendances(filter)) {
                is Result.Success -> _state.update { it.copy(
                    attendances = r.data.items,
                    totalCount = r.data.totalCount,
                    isLoading = false
                )}
                is Result.Error -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectGroup(groupId: String?) {
        _state.update { it.copy(selectedGroupId = groupId) }
        loadAttendances()
        if (groupId != null) loadGroupDetails(groupId)
    }

    fun selectStatus(status: Int?) {
        _state.update { it.copy(selectedStatus = status) }
        loadAttendances()
    }

    fun selectTab(tab: AttendanceTab) {
        _state.update { it.copy(activeTab = tab) }
    }

    private fun loadGroupDetails(groupId: String) {
        scope.launch {
            when (val r = repository.getGroupAttendanceDetails(groupId)) {
                is Result.Success -> _state.update { it.copy(groupDetails = r.data) }
                is Result.Error -> {}
            }
        }
    }
}

enum class AttendanceTab { BY_GROUPS, BY_STUDENTS }

data class TeacherAttendanceState(
    val isLoading: Boolean = false,
    val groups: List<GroupDto> = emptyList(),
    val groupStats: List<AttendanceGroupStatsDto> = emptyList(),
    val groupDetails: AttendanceGroupDetailsDto? = null,
    val attendances: List<AttendanceRecordDto> = emptyList(),
    val totalCount: Int = 0,
    val selectedGroupId: String? = null,
    val selectedStatus: Int? = null,
    val activeTab: AttendanceTab = AttendanceTab.BY_GROUPS
)