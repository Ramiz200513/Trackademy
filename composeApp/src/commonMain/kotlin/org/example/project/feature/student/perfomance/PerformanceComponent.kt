package org.example.project.feature.performance

import kotlinx.coroutines.flow.StateFlow
import org.example.project.feature.student.perfomance.PerformanceIntent
import org.example.project.feature.student.perfomance.PerformanceState

interface PerformanceComponent {
    val state: StateFlow<PerformanceState>
    fun onIntent(intent: PerformanceIntent)
}