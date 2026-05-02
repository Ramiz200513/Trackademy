package org.example.project.data.mapper

import org.example.project.data.remote.dto.*
import org.example.project.domain.models.*

fun PerformanceAssignmentDto.toDomain(): Assignment = Assignment(
    id = this.assignmentId,
    description = this.description,
    subjectName = this.subjectName,
    dueDate = this.dueDate.take(10),
    assignedDate = this.dueDate.take(10),
    status = this.status,
    isOverdue = this.isOverdue,
    groupName = "—"
)

fun PerformanceDto.toDomain(): Performance {
    return Performance(
        averageGrade = this.averageGrade ?: 0.0,
        attendanceRate = this.attendanceRate?.toInt() ?: 0,
        activeAssignmentsCount = this.activeAssignments ?: 0,
        assignments = this.activeAssignmentsList.map { it.toDomain() },
        grades = this.recentGrades.map { it.toDomain() }
    )
}
fun AssignmentItemDto.toDomain(): Assignment = Assignment(
    id = this.assignmentId,
    description = this.description,
    subjectName = this.subject?.name ?: "—",
    dueDate = this.dueDate?.take(10) ?: "—",
    assignedDate = this.assignedDate?.take(10) ?: "—",
    status = when (this.status) {
        1 -> "pending"
        2 -> "submitted"
        3 -> "graded"
        else -> "overdue"
    },
    isOverdue = this.status == 4,
    submissionId = this.submissionId,
    score = this.score,
    groupName = this.group?.name ?: "—"
)
fun GradeDto.toDomain(): Grade {
    return Grade(
        subjectName = this.subjectName,
        grade = this.grade,
        date = this.gradedAt
    )
}