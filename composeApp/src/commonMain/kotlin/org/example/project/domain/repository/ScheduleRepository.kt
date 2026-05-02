package org.example.project.domain.repository

import org.example.project.core.domain.NetworkError
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.LessonDto
import org.example.project.data.remote.dto.LessonRequest
import org.example.project.data.remote.dto.MarkAttendanceRequest
import org.example.project.domain.models.Group
import org.example.project.domain.models.Lesson
import org.example.project.domain.models.Room

interface ScheduleRepository {
    suspend fun getLessons(params: LessonRequest): Result<List<Lesson>, NetworkError>
    suspend fun getScheduleIds(orgId: String): Result<List<String>, NetworkError>
    suspend fun getGroups(orgId: String): Result<List<Group>, NetworkError>
    suspend fun getRooms(orgId: String): Result<List<Room>, NetworkError>
    suspend fun getLessonById(lessonId: String): Result<LessonDto, NetworkError>
    suspend fun markAttendance(request: MarkAttendanceRequest): Result<Unit, NetworkError>
}