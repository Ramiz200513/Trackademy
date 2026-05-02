package org.example.project.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.example.project.domain.repository.AuthRepository
import org.example.project.domain.repository.PerformanceRepository
import org.example.project.domain.repository.ScheduleRepository
import org.example.project.feature.auth.AuthComponent
import org.example.project.feature.student.perfomance.DefaultPerformanceComponent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.example.project.domain.repository.AssignmentRepository
import org.example.project.domain.repository.DashboardRepository
import org.example.project.domain.repository.MaterialRepository
import org.example.project.domain.repository.ProfileRepository
import org.example.project.domain.repository.TeacherRepository
import org.example.project.domain.utils.FileSaver
import org.example.project.core.network.Result
class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<Config>()
    private val authRepository: AuthRepository by inject()
    private val performanceRepository: PerformanceRepository by inject()
    private val scheduleRepository: ScheduleRepository by inject()
    private val profileRepository: ProfileRepository by inject()
    private val settings: Settings by inject()
    private val materialRepository: MaterialRepository by inject()
    private val assignmentRepository: AssignmentRepository by inject()
    private val teacherRepository : TeacherRepository by inject()
    private val dashboardRepository: DashboardRepository by inject()
    private val fileSaver: FileSaver by inject()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = if (settings.getString("token", "").isNotBlank()) {
            Config.Main(settings.getString("orgId", ""))
        } else {
            Config.Auth
        },
        handleBackButton = true,
        childFactory = ::createChild
    )
    init {
        val token = settings.getString("token", "")
        if (token.isNotBlank()) {
            kotlinx.coroutines.MainScope().launch {
                when (val result = authRepository.getCurrentUser()) {
                    is Result.Success -> {
                        settings.putInt("userRole", result.data.role)
                        println("INIT: role refreshed = ${result.data.role}")
                    }
                    is Result.Error -> {}
                }
            }
        }
    }
    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child {
        return when (config) {
            is Config.Auth -> RootComponent.Child.Auth(
                AuthComponent(
                    componentContext = componentContext,
                    repository = authRepository,
                    onRootNavigate = {
                        val orgId = settings.getString("orgId", "")
                        navigation.replaceCurrent(Config.Main(orgId))
                    }
                )
            )
            is Config.Main -> RootComponent.Child.Main(
                DefaultMainComponent(
                    componentContext = componentContext,
                    authRepository = authRepository,
                    performanceRepository = performanceRepository,
                    scheduleRepository = scheduleRepository,
                    assignmentRepository = assignmentRepository,
                    materialRepository = materialRepository,
                    profileRepository = profileRepository,
                    fileSaver = fileSaver,
                    settings = settings,
                    orgId = config.orgId,
                    onLogout = {
                        settings.remove("token")
                        settings.remove("orgId")
                        navigation.replaceCurrent(Config.Auth)
                    },
                    userRole = run {

                        val saved = settings.getInt("userRole", 0)
                        println("ROOT createChild: userRole=$saved")
                        saved
                    },
                    teacherRepository = teacherRepository,
                    dashboardRepository = dashboardRepository
                )
            )
            is Config.Performance -> RootComponent.Child.Performance(
                DefaultPerformanceComponent(
                    componentContext = componentContext,
                    repository = performanceRepository,
                    authRepository = authRepository,
                    onNavigateToAssignment = {}
                )
            )
        }
    }

    override fun onScheduleTabClicked() {
        val orgId = settings.getString("orgId", "")
        navigation.replaceCurrent(Config.Main(orgId))
    }

    override fun onPerformanceTabClicked() {
        navigation.replaceCurrent(Config.Performance)
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object Auth : Config
        @Serializable data class Main(val orgId: String) : Config
        @Serializable data object Performance : Config
    }
}