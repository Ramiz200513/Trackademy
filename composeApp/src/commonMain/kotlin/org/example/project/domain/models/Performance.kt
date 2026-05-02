package org.example.project.domain.models

data class Performance(
    val averageGrade: Double,
    val attendanceRate: Int,
    val activeAssignmentsCount: Int,
    val assignments: List<Assignment> = emptyList(),
    val grades: List<Grade> = emptyList()
)