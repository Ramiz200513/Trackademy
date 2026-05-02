package org.example.project.domain.models

data class Assignment(
    val id: String,
    val description: String,
    val subjectName: String,
    val groupName: String,
    val dueDate: String,
    val assignedDate: String,
    val status: String,
    val isOverdue: Boolean,
    val submissionId: String? = null,
    val score: Int? = null
)