// feature/performance/PerformanceComponent.kt
package org.example.project.feature.performance

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.domain.repository.PerformanceRepository
import org.example.project.core.network.Result
import org.example.project.domain.repository.AuthRepository
import org.example.project.feature.student.perfomance.PerformanceIntent
import org.example.project.feature.student.perfomance.PerformanceState

interface PerformanceComponent {
    val state: StateFlow<PerformanceState>
    fun onIntent(intent: PerformanceIntent)
}
class DefaultPerformanceComponent(
    componentContext: ComponentContext,
    private val repository: PerformanceRepository,
    private val authRepository: AuthRepository,
    private val onNavigateToAssignment: (String) -> Unit
) : PerformanceComponent, ComponentContext by componentContext {
    private val scope = coroutineScope()
    private val _state = MutableStateFlow(PerformanceState())
    override val state = _state.asStateFlow()

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
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = repository.getPerformanceData()) {
                is Result.Success -> {
                    val d = result.data
                    _state.update { it.copy(
                        isLoading = false,
                        avgGrade = d.averageGrade,
                        attendance = d.attendanceRate,
                        activeCount = d.activeAssignmentsCount,
                        assignments = d.assignments,
                        grades = d.grades
                    ) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = "Ошибка сети") }
                }
            }
        }
    }
}