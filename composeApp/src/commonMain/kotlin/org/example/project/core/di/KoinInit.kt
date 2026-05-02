package org.example.project.core.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(appModules)
    }
}