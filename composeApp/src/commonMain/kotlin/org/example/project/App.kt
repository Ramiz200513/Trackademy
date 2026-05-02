package org.example.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import org.example.project.feature.auth.ui.AuthContent
import org.example.project.root.RootComponent
import org.example.project.root.ui.MainContent
import org.example.project.feature.student.perfomance.ui.PerformanceScreen

@Composable
fun App(rootComponent: RootComponent) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Children(
                stack = rootComponent.stack,
                animation = stackAnimation(fade())
            ) { child ->
                when (val instance = child.instance) {
                    is RootComponent.Child.Auth -> {
                        AuthContent(component = instance.component)
                    }
                    is RootComponent.Child.Main -> {
                        MainContent(component = instance.component)
                    }
                    is RootComponent.Child.Performance -> {
                        PerformanceScreen(component = instance.component)
                    }
                }
            }
        }
    }
}