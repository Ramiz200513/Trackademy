package org.example.project.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import org.example.project.domain.repository.AssignmentRepository
import org.example.project.domain.repository.AuthRepository
import org.example.project.domain.repository.DashboardRepository
import org.example.project.domain.repository.MaterialRepository
import org.example.project.domain.repository.PerformanceRepository
import org.example.project.domain.repository.ProfileRepository
import org.example.project.domain.repository.ScheduleRepository
import org.example.project.domain.repository.TeacherRepository
import org.example.project.domain.utils.FileSaver
import org.example.project.feature.student.assignment.AssignmentComponent
import org.example.project.feature.student.materials.MaterialComponent
import org.example.project.feature.student.perfomance.DefaultPerformanceComponent
import org.example.project.feature.profile.ProfileComponent
import org.example.project.feature.schedule.ScheduleComponent
import org.example.project.feature.teacher.TeacherAssignmentComponent
import org.example.project.feature.teacher.TeacherAttendanceComponent
import org.example.project.feature.teacher.dashboard.DashboardComponent

class DefaultMainComponent(
    componentContext: ComponentContext,
    private val authRepository: AuthRepository,
    private val performanceRepository: PerformanceRepository,
    private val scheduleRepository: ScheduleRepository,
    private val orgId: String,
    private val assignmentRepository: AssignmentRepository,
    private val materialRepository: MaterialRepository,
    private val profileRepository: ProfileRepository,
    private val fileSaver: FileSaver,
    private val settings: Settings,
    private val onLogout: () -> Unit,
    private val teacherRepository: TeacherRepository,
    override val userRole: Int,
    private val dashboardRepository: DashboardRepository,

    ) : MainComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, MainComponent.MainChild>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = Config.Schedule,
        handleBackButton = true,
        childFactory = ::createChild
    )
    init {
        println("MAIN COMPONENT INIT, orgId=$orgId, userRole=$userRole")
    }
    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): MainComponent.MainChild {
        return when (config) {
            is Config.Schedule -> MainComponent.MainChild.Schedule(
                ScheduleComponent(
                    componentContext = componentContext,
                    repository = scheduleRepository,
                    orgId = orgId,
                    userRole = userRole
                )
            )
            is Config.TeacherAttendance -> MainComponent.MainChild.TeacherAttendance(
                TeacherAttendanceComponent(componentContext, teacherRepository, orgId,onLogout)
            )
            is Config.TeacherAssignments -> MainComponent.MainChild.TeacherAssignments(
                TeacherAssignmentComponent(componentContext, teacherRepository, orgId,onLogout)
            )
            is Config.Performance -> {
                if (userRole == 3) {
                    MainComponent.MainChild.Dashboard(
                        DashboardComponent(componentContext, dashboardRepository,onLogout)
                    )
                } else {
                    MainComponent.MainChild.Performance(
                        DefaultPerformanceComponent(
                            componentContext = componentContext,
                            repository = performanceRepository,
                            authRepository = authRepository,
                            onNavigateToAssignment = {}
                        )
                    )
                }
            }

            is Config.Assignments -> MainComponent.MainChild.Assignments(
                AssignmentComponent(
                    componentContext = componentContext,
                    repository = assignmentRepository,
                    fileSaver = fileSaver
                )
            )
            is Config.Profile -> MainComponent.MainChild.Profile(
                ProfileComponent(
                    componentContext = componentContext,
                    repository = profileRepository,
                    settings = settings,
                    onLogout = onLogout
                )
            )
            is Config.Materials -> MainComponent.MainChild.Materials(
                MaterialComponent(
                    componentContext = componentContext,
                    repository = materialRepository,  // ← правильный репозиторий
                    fileSaver = fileSaver,
                    settings = settings
                )
            )
        }
    }

    override fun onTabClick(tab: MainComponent.MainTab) {
        when (tab) {
            MainComponent.MainTab.SCHEDULE -> navigation.bringToFront(Config.Schedule)
            MainComponent.MainTab.PERFORMANCE -> navigation.bringToFront(Config.Performance)
            MainComponent.MainTab.HOME -> {
                if (userRole == 1) navigation.bringToFront(Config.Assignments)
                else navigation.bringToFront(Config.TeacherAssignments)
            }
            MainComponent.MainTab.MATERIALS -> navigation.bringToFront(Config.Materials)
            MainComponent.MainTab.PROFILE -> navigation.bringToFront(Config.Profile)
            MainComponent.MainTab.ATTENDANCE -> navigation.bringToFront(Config.TeacherAttendance)
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object Profile: Config
        @Serializable data object Assignments : Config
        @Serializable data object Schedule    : Config
        @Serializable data object Performance : Config
        @Serializable data object Materials   : Config
        @Serializable data object TeacherAttendance : Config
        @Serializable data object TeacherAssignments : Config
    }
}