package org.example.project.feature.teacher.dashboard

import com.arkivanov.decompose.ComponentContext
import org.example.project.core.network.Result
import org.example.project.core.presentation.BaseComponent
import org.example.project.core.session.SessionStorage
import org.example.project.core.utils.toInitials
import org.example.project.data.remote.dto.DashboardDto
import org.example.project.domain.repository.DashboardRepository

class DashboardComponent(
    componentContext: ComponentContext,
    private val repository: DashboardRepository,
    val onLogout: () -> Unit,
    session: SessionStorage
) : BaseComponent<DashboardState, Nothing>(componentContext, DashboardState()) {

    init {
        val fullName = session.fullName ?: "Преподаватель"
        updateState { copy(userInitials = fullName.toInitials()) }
        load()
    }

    fun load() {
        launch {
            updateState { copy(isLoading = true, error = false) }
            when (val result = repository.getTeacherDashboard()) {
                is Result.Success -> updateState { copy(isLoading = false, data = result.data) }
                is Result.Error -> updateState { copy(isLoading = false, error = true) }
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
