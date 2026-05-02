package org.example.project.feature.student.perfomance

import org.example.project.domain.models.Assignment
import org.example.project.domain.models.Grade

data class PerformanceState(
    val isLoading: Boolean = false,
    val userName: String = "Студент",
    val avgGrade: Double = 0.0,
    val attendance: Int = 0,
    val activeCount: Int = 0,
    val assignments: List<Assignment> = emptyList(),
    val grades: List<Grade> = emptyList(),
    val error: String? = null,
    val userInitials: String = "??"
)
sealed interface PerformanceIntent {
    data object Refresh : PerformanceIntent
    data class ClickAssignment(val id: String) : PerformanceIntent
}