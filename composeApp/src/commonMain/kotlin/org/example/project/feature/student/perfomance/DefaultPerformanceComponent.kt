package org.example.project.feature.student.perfomance

import com.arkivanov.decompose.ComponentContext
import kotlinx.coroutines.async
import org.example.project.core.network.Result
import org.example.project.core.presentation.BaseComponent
import org.example.project.core.ui.error.toUserMessage
import org.example.project.core.utils.toInitials
import org.example.project.domain.repository.AuthRepository
import org.example.project.domain.repository.PerformanceRepository
import org.example.project.feature.performance.PerformanceComponent

class DefaultPerformanceComponent(
    componentContext: ComponentContext,
    private val repository: PerformanceRepository,
    private val authRepository: AuthRepository,
    private val onNavigateToAssignment: (String) -> Unit
) : BaseComponent<PerformanceState, Nothing>(componentContext, PerformanceState()),
    PerformanceComponent {

    init {
        onIntent(PerformanceIntent.Refresh)
    }

    override fun onIntent(intent: PerformanceIntent) {
        when (intent) {
            is PerformanceIntent.Refresh -> loadData()
            is PerformanceIntent.ClickAssignment -> onNavigateToAssignment(intent.id)
        }
    }

    private fun loadData() {
        launch {
            updateState { copy(isLoading = true, error = null) }

            // Both requests run in parallel
            val dashboardDeferred = async { repository.getPerformanceData() }
            val userDeferred = async { authRepository.getCurrentUser() }
            val dashboardResult = dashboardDeferred.await()
            val userResult = userDeferred.await()

            when (dashboardResult) {
                is Result.Success -> {
                    val data = dashboardResult.data
                    val fullName = (userResult as? Result.Success)?.data?.fullName ?: "Студент"
                    updateState {
                        copy(
                            isLoading = false,
                            avgGrade = data.averageGrade,
                            attendance = data.attendanceRate,
                            activeCount = data.activeAssignmentsCount,
                            assignments = data.assignments,
                            grades = data.grades,
                            userName = fullName,
                            userInitials = fullName.toInitials()
                        )
                    }
                }
                is Result.Error -> updateState {
                    copy(isLoading = false, error = dashboardResult.error.toUserMessage())
                }
            }
        }
    }
}
