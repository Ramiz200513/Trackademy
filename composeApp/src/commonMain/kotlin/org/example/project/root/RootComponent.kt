package org.example.project.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import org.example.project.feature.student.assignment.AssignmentComponent
import org.example.project.feature.auth.AuthComponent
import org.example.project.feature.student.materials.MaterialComponent
import org.example.project.feature.performance.PerformanceComponent
import org.example.project.feature.profile.ProfileComponent
import org.example.project.feature.schedule.ScheduleComponent
import org.example.project.feature.teacher.TeacherAssignmentComponent
import org.example.project.feature.teacher.TeacherAttendanceComponent
import org.example.project.feature.teacher.dashboard.DashboardComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>
    fun onScheduleTabClicked()
    fun onPerformanceTabClicked()

    sealed class Child {
        class Auth(val component: AuthComponent) : Child()
        class Performance(val component: PerformanceComponent) : Child()
        class Main(val component: MainComponent) : Child()
    }
}

interface MainComponent {
    val stack: Value<ChildStack<*, MainChild>>
    val userRole: Int
    fun onTabClick(tab: MainTab)

    enum class MainTab { SCHEDULE, PERFORMANCE, HOME, MATERIALS, PROFILE,ATTENDANCE }

    sealed class MainChild {
        class Profile(val component: ProfileComponent): MainChild()
        data class Materials(val component: MaterialComponent) : MainChild()
        class Schedule(val component: ScheduleComponent) : MainChild()
        class Performance(val component: PerformanceComponent) : MainChild()
        class Assignments(val component: AssignmentComponent) : MainChild()
        class TeacherAttendance(val component: TeacherAttendanceComponent) : MainChild()
        class TeacherAssignments(val component: TeacherAssignmentComponent) : MainChild()
        class Dashboard(val component: DashboardComponent) : MainChild()
    }
}