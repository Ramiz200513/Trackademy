package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.resume
import org.example.project.root.DefaultRootComponent
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    val lifecycle = LifecycleRegistry()
    val root = DefaultRootComponent(componentContext = DefaultComponentContext(lifecycle))
    lifecycle.resume()
    return ComposeUIViewController { App(rootComponent = root) }
}
