package org.example.project.feature.schedule

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.core.network.Result
import org.example.project.data.remote.dto.LessonRequest
import org.example.project.domain.repository.ScheduleRepository
import kotlinx.datetime.*
import org.example.project.data.remote.dto.AttendanceMarkDto
import org.example.project.data.remote.dto.MarkAttendanceRequest

class ScheduleComponent(
    componentContext: ComponentContext,
    private val repository: ScheduleRepository,
    private val orgId: String,
    val userRole: Int = 0
) : ComponentContext by componentContext {

    private val scope = coroutineScope()

    private val today: LocalDate
        get() = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

    private val _state = MutableStateFlow(
        ScheduleState(
            selectedDate = today,
            weekStart = getWeekStart(today)
        )
    )
    val state: StateFlow<ScheduleState> = _state.asStateFlow()

    init {
        println("SCHEDULE COMPONENT INIT, orgId=$orgId")
        generateCalendarDays()
        loadLessons()
    }

    fun handleIntent(intent: ScheduleIntent) {
        when (intent) {
            is ScheduleIntent.SelectDate -> {
                _state.update { it.copy(selectedDate = intent.date) }
                loadLessons()
            }
            is ScheduleIntent.OpenLesson -> loadLessonDetail(intent.lessonId)
            is ScheduleIntent.CloseLesson -> _state.update { it.copy(selectedLesson = null) }
            is ScheduleIntent.SaveAttendance -> saveAttendance(intent.attendances)
            is ScheduleIntent.NextWeek -> shiftWeek(1)
            is ScheduleIntent.PrevWeek -> shiftWeek(-1)
            is ScheduleIntent.Refresh -> loadLessons()
            is ScheduleIntent.SetViewMode -> {
                _state.update { it.copy(viewMode = intent.mode) }
            }
            is ScheduleIntent.PrevMonth -> {
                val newDate = _state.value.selectedDate.minus(1, DateTimeUnit.MONTH)
                _state.update { it.copy(selectedDate = newDate, weekStart = getWeekStart(newDate)) }
                generateCalendarDays()
            }
            is ScheduleIntent.NextMonth -> {
                val newDate = _state.value.selectedDate.plus(1, DateTimeUnit.MONTH)
                _state.update { it.copy(selectedDate = newDate, weekStart = getWeekStart(newDate)) }
                generateCalendarDays()
            }
        }
    }
    private fun loadLessonDetail(lessonId: String) {
        scope.launch {
            _state.update { it.copy(isLessonLoading = true) }
            when (val r = repository.getLessonById(lessonId)) {
                is Result.Success -> _state.update {
                    it.copy(isLessonLoading = false, selectedLesson = r.data)
                }
                is Result.Error -> _state.update { it.copy(isLessonLoading = false) }
            }
        }
    }

    private fun saveAttendance(attendances: List<AttendanceMarkDto>) {
        val lessonId = _state.value.selectedLesson?.id ?: return
        scope.launch {
            _state.update { it.copy(isSavingAttendance = true) }
            val request = MarkAttendanceRequest(lessonId = lessonId, attendances = attendances)
            when (repository.markAttendance(request)) {
                is Result.Success -> {
                    // перезагрузить урок чтобы обновить статусы
                    loadLessonDetail(lessonId)
                    _state.update { it.copy(isSavingAttendance = false) }
                }
                is Result.Error -> _state.update { it.copy(isSavingAttendance = false) }
            }
        }
    }
    private fun shiftWeek(direction: Int) {
        val newWeekStart = _state.value.weekStart.plus(direction * 7, DateTimeUnit.DAY)
        _state.update { it.copy(weekStart = newWeekStart) }
        generateCalendarDays()
    }

    private fun getWeekStart(date: LocalDate): LocalDate {
        val dow = date.dayOfWeek.ordinal // Mon=0 .. Sun=6
        return date.minus(dow, DateTimeUnit.DAY)
    }

    private fun generateCalendarDays() {
        val ws = _state.value.weekStart
        val days = (0..6).map { ws.plus(it, DateTimeUnit.DAY) }
        _state.update { it.copy(calendarDays = days) }
    }

    private fun loadLessons() {
        scope.launch {
            println("LOAD LESSONS CALLED")
            _state.update { it.copy(isLoading = true) }

            val dateStr = _state.value.selectedDate.toString()

            // убрать getScheduleIds — он не нужен

            val result = repository.getLessons(
                LessonRequest(
                    organizationId = orgId,
                    fromDate = dateStr,
                    toDate = dateStr,
                    pageNumber = 1,
                    pageSize = 100
                )
            )
            when (result) {
                is Result.Success -> _state.update {
                    it.copy(isLoading = false, lessons = result.data, error = null)
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = "Ошибка загрузки уроков")
                }
            }
        }
    }
}