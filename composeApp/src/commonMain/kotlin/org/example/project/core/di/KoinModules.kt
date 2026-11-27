package org.example.project.core.di

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import org.example.project.core.coroutines.DefaultDispatcherProvider
import org.example.project.core.coroutines.DispatcherProvider
import org.example.project.core.network.createHttpClient
import org.example.project.core.session.SessionStorage
import org.example.project.data.repository.*
import org.example.project.domain.repository.*
import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single<Settings> { Settings() }
    single { SessionStorage(get()) }
    single<DispatcherProvider> { DefaultDispatcherProvider() }
}

val networkModule: Module = module {
    single<HttpClient> { createHttpClient(session = get()) }
}

val repositoryModule: Module = module {
    single<AuthRepository> { AuthRepositoryImpl(httpClient = get(), session = get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(httpClient = get(), settings = get()) }
    single<TeacherRepository> { TeacherRepositoryImpl(get(), get()) }
    single<MaterialRepository> { MaterialRepositoryImpl(httpClient = get(), settings = get()) }
    single<AssignmentRepository> { AssignmentRepositoryImpl(httpClient = get(), settings = get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }
    single<ScheduleRepository> { ScheduleRepositoryImpl(httpClient = get(), settings = get()) }
    single<PerformanceRepository> { PerformanceRepositoryImpl(httpClient = get(), settings = get()) }
}

val appModules: List<Module> = listOf(coreModule, networkModule, repositoryModule)
