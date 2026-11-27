package org.example.project.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.example.project.core.network.clearAuthTokens
import org.example.project.core.network.onSuccess
import org.example.project.core.session.SessionStorage
import org.example.project.core.session.UserRole
import org.example.project.domain.repository.AuthRepository
import org.example.project.domain.utils.FileSaver
import org.example.project.feature.auth.AuthComponent
import org.example.project.feature.student.perfomance.DefaultPerformanceComponent
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class DefaultRootComponent(
    componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<Config>()
    private val settings: Settings by inject()
    private val session: SessionStorage by inject()
    private val httpClient: HttpClient by inject()
    private val fileSaver: FileSaver by inject()
    private val authRepository: AuthRepository by inject()

    // Bound to the component lifecycle: cancelled automatically on destroy
    private val scope = coroutineScope()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = Config.serializer(),
        initialConfiguration = if (session.isLoggedIn) {
            Config.Main(orgId = session.organizationId, userRole = session.userRole.code)
        } else {
            Config.Auth
        },
        handleBackButton = true,
        childFactory = ::createChild
    )

    init {
        if (session.isLoggedIn) refreshCurrentUser()
    }

    /** Keeps role and name in sync with the server on app start. */
    private fun refreshCurrentUser() {
        scope.launch {
            authRepository.getCurrentUser().onSuccess { user ->
                session.userRole = UserRole.fromCode(user.role)
                session.fullName = user.fullName
            }
        }
    }

    private fun logout() {
        session.clear()
        httpClient.clearAuthTokens()
        navigation.replaceAll(Config.Auth)
    }

    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): RootComponent.Child = when (config) {
        is Config.Auth -> RootComponent.Child.Auth(
            AuthComponent(
                componentContext = componentContext,
                repository = authRepository,
                session = session,
                onRootNavigate = {
                    navigation.replaceCurrent(
                        Config.Main(orgId = session.organizationId, userRole = session.userRole.code)
                    )
                }
            )
        )
        is Config.Main -> RootComponent.Child.Main(
            DefaultMainComponent(
                componentContext = componentContext,
                authRepository = authRepository,
                performanceRepository = get(),
                scheduleRepository = get(),
                assignmentRepository = get(),
                materialRepository = get(),
                profileRepository = get(),
                teacherRepository = get(),
                dashboardRepository = get(),
                fileSaver = fileSaver,
                settings = settings,
                session = session,
                orgId = config.orgId,
                onLogout = ::logout,
                userRole = config.userRole
            )
        )
        is Config.Performance -> RootComponent.Child.Performance(
            DefaultPerformanceComponent(
                componentContext = componentContext,
                repository = get(),
                authRepository = authRepository,
                onNavigateToAssignment = {}
            )
        )
    }

    override fun onScheduleTabClicked() {
        navigation.replaceCurrent(Config.Main(session.organizationId, session.userRole.code))
    }

    override fun onPerformanceTabClicked() {
        navigation.replaceCurrent(Config.Performance)
    }

    @Serializable
    private sealed interface Config {
        @Serializable data object Auth : Config
        @Serializable data class Main(val orgId: String, val userRole: Int) : Config
        @Serializable data object Performance : Config
    }
}
