package org.example.project

import android.app.Application
import org.example.project.core.di.appModules
import org.example.project.data.utils.AndroidFileSaver
import org.example.project.domain.utils.FileSaver
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val androidModule = module {
            single<FileSaver> { AndroidFileSaver(androidContext()) }
        }

        startKoin {
            androidContext(this@MainApplication)
            modules(appModules + androidModule)
        }
    }
}