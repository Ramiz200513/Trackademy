package org.example.project.core.di

import org.example.project.data.utils.IosFileSaver
import org.example.project.domain.utils.FileSaver
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoin() {
    val iosModule = module {
        single<FileSaver> { IosFileSaver() }
    }

    startKoin {
        modules(appModules + iosModule)
    }
}