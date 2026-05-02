package org.example.project.domain.models


data class AssignmentGroup(
    val status: String,
    val assignments: List<Assignment>
)