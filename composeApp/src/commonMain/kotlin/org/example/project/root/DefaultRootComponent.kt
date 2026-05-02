package org.example.project.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.example.project.core.di.appModules
import org.example.project.domain.repository.AuthRepository
import org.koin.core.component.get
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
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<Config>()
    private val settings: Settings by inject()
    private val fileSaver: FileSaver by inject()
    private fun logout() {
        settings.clear()
        stopKoin()
        startKoin { modules(appModules) }
        navigation.replaceAll(Config.Auth)
    }
    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = if (settings.getString("token", "").isNotBlank()) {
            Config.Main(
                orgId = settings.getString("orgId", ""),
                userRole = getSafeRole()
            )
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
                val authRepo: AuthRepository = get()
                when (val result = authRepo.getCurrentUser()) {
                    is Result.Success -> {
                        settings.putInt("userRole", result.data.role)
                        settings.putString("fullName", result.data.fullName)
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
                    repository = get(), // Свежий репозиторий
                    settings = settings,
                    onRootNavigate = {
                        val orgId = settings.getString("orgId", "")
                        val role = getSafeRole()
                        navigation.replaceCurrent(Config.Main(orgId = orgId, userRole = role))
                    }
                )
            )
            is Config.Main -> RootComponent.Child.Main(
                DefaultMainComponent(
                    componentContext = componentContext,
                    // ПЕРЕДАЕМ get() ДЛЯ ВСЕХ, чтобы Koin выдал новые объекты после рестарта
                    authRepository = get(),
                    performanceRepository = get(),
                    scheduleRepository = get(),
                    assignmentRepository = get(),
                    materialRepository = get(),
                    profileRepository = get(),
                    teacherRepository = get(),
                    dashboardRepository = get(),
                    fileSaver = fileSaver,
                    settings = settings,
                    orgId = config.orgId,
                    onLogout = ::logout,
                    userRole = config.userRole
                )
            )
            is Config.Performance -> RootComponent.Child.Performance(
                DefaultPerformanceComponent(
                    componentContext = componentContext,
                    repository = get(),
                    authRepository = get(),
                    onNavigateToAssignment = {}
                )
            )
        }
    }


    override fun onScheduleTabClicked() {
        val orgId = settings.getString("orgId", "")
        val role = settings.getInt("userRole", 0)
        navigation.replaceCurrent(Config.Main(orgId, role))
    }

    override fun onPerformanceTabClicked() {
        navigation.replaceCurrent(Config.Performance)
    }
    private fun getSafeRole(): Int {
        return try {
            settings.getInt("userRole", 0)
        } catch (e: Exception) {
            0
        }
    }
    @Serializable
    private sealed interface Config {
        @Serializable data object Auth : Config
        @Serializable data class Main(val orgId: String, val userRole: Int) : Config
        @Serializable data object Performance : Config
    }
}