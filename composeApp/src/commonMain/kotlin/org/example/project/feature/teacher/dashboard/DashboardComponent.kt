package org.example.project.feature.teacher.dashboard

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.russhwolf.settings.Settings // НЕ ЗАБУДЬ ИМПОРТ
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.DashboardDto
import org.example.project.data.remote.dto.TodayLessonDto
import org.example.project.domain.repository.DashboardRepository

class DashboardComponent(
    componentContext: ComponentContext,
    private val repository: DashboardRepository,
    val onLogout: () -> Unit,
    private val settings: Settings // 1. ДОБАВИЛИ SETTINGS
) : ComponentContext by componentContext {

    private val scope = coroutineScope()
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        val fullName = settings.getString("fullName", "Преподаватель")
        val initials = fullName.split(" ").filter { it.isNotBlank() }
            .mapNotNull { it.firstOrNull()?.toString()?.uppercase() }.take(2).joinToString("")

        _state.update { it.copy(userInitials = initials) }

        load()
    }

    fun load() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = false) }
            when (val r = repository.getTeacherDashboard()) {
                is Result.Success -> _state.update {
                    it.copy(isLoading = false, data = r.data)
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = true)
                }
            }
        }
    }
}

data class DashboardState(
    val isLoading: Boolean = false,
    val error: Boolean = false,
    val data: DashboardDto? = null,
    val userInitials: String = "??"
)