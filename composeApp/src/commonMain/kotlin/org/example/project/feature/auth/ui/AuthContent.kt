package org.example.project.feature.auth.ui

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.example.project.feature.auth.AuthComponent
import org.example.project.feature.auth.AuthMode

@Composable
fun AuthContent(component: AuthComponent) {
    val state by component.state.collectAsState()

    Crossfade(targetState = state.authMode) { mode ->
        when (mode) {
            AuthMode.LOGIN -> {
                LoginScreen(component = component)
            }
            AuthMode.REGISTER -> {
                RegistrationScreen(component = component)
            }
        }
    }
}