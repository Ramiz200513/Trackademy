package org.example.project.data.mapper

import org.example.project.data.remote.dto.AssignmentGroupDto
import org.example.project.data.remote.dto.AssignmentItemDto
import org.example.project.data.remote.dto.AssignmentSubjectDto
import org.example.project.data.remote.dto.GradeDto
import org.example.project.data.remote.dto.PerformanceDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MappersTest {

    @Test
    fun performanceDtoWithNullsMapsToSafeDefaults() {
        val dto = PerformanceDto(averageGrade = null, attendanceRate = null, activeAssignments = null)

        val performance = dto.toDomain()

        assertEquals(0.0, performance.averageGrade)
        assertEquals(0, performance.attendanceRate)
        assertEquals(0, performance.activeAssignmentsCount)
    }

    @Test
    fun performanceDtoMapsGradesAndTruncatesAttendance() {
        val dto = PerformanceDto(
            averageGrade = 4.5,
            attendanceRate = 87.9,
            activeAssignments = 2,
            recentGrades = listOf(GradeDto("Math", 5, "2025-01-10"))
        )

        val performance = dto.toDomain()

        assertEquals(4.5, performance.averageGrade)
        assertEquals(87, performance.attendanceRate)
        assertEquals("Math", performance.grades.single().subjectName)
    }

    @Test
    fun assignmentItemMapsStatusCodes() {
        assertEquals("pending", assignment(status = 1).toDomain().status)
        assertEquals("submitted", assignment(status = 2).toDomain().status)
        assertEquals("graded", assignment(status = 3).toDomain().status)
        assertEquals("overdue", assignment(status = 4).toDomain().status)
        assertTrue(assignment(status = 4).toDomain().isOverdue)
        assertFalse(assignment(status = 1).toDomain().isOverdue)
    }

    @Test
    fun assignmentItemTrimsDatesAndFillsMissingRefs() {
        val mapped = assignment(status = 1, dueDate = "2025-03-01T10:00:00Z").toDomain()
        assertEquals("2025-03-01", mapped.dueDate)
        assertEquals("—", mapped.assignedDate)

        val withRefs = assignment(
            status = 1,
            group = AssignmentGroupDto("g1", "Group A"),
            subject = AssignmentSubjectDto("s1", "Physics")
        ).toDomain()
        assertEquals("Group A", withRefs.groupName)
        assertEquals("Physics", withRefs.subjectName)
    }

    private fun assignment(
        status: Int?,
        dueDate: String? = null,
        group: AssignmentGroupDto? = null,
        subject: AssignmentSubjectDto? = null
    ) = AssignmentItemDto(
        assignmentId = "a1",
        description = "Homework",
        dueDate = dueDate,
        status = status,
        group = group,
        subject = subject
    )
}
