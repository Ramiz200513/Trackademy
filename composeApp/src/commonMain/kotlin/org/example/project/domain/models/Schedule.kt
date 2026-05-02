package org.example.project.domain.models
data class Lesson(
    val id: String,
    val subjectName: String,
    val teacherName: String,
    val roomName: String,
    val startTime: String,
    val endTime: String,
    val groupName: String,
    val date: String
)

data class CalendarDay(
    val date: String,
    val dayOfWeek: String,
    val dayOfMonth: String
)
data class Group(
    val id: String,
    val name: String
)

data class Room(
    val id: String,
    val name: String
)