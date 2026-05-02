package org.example.project.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.ui.theme.*
import org.example.project.feature.auth.AuthComponent
import org.example.project.feature.auth.AuthIntent
import org.example.project.feature.component.OrganizationDropdown
import org.example.project.feature.component.TrackademyTextField
import org.jetbrains.compose.resources.painterResource
import trackademy.composeapp.generated.resources.Res
import trackademy.composeapp.generated.resources.compose_multiplatform

@Composable
fun LoginScreen(component: AuthComponent) {
    val state by component.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.compose_multiplatform),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Unspecified
            )

            Spacer(Modifier.height(24.dp))

            // --- ЗАГОЛОВКИ ---
            Text(
                text = "С возвращением!",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Войдите в систему, чтобы продолжить обучение",
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // --- КАРТОЧКА ФОРМЫ ---
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Поле Логина
                    TrackademyTextField(
                        value = state.loginInput,
                        onValueChange = {
                            component.handleIntent(AuthIntent.UpdateLoginFields(it, state.passwordInput, state.selectedOrgId))
                        },
                        label = "Логин",
                        placeholder = "Введите ваш логин"
                    )

                    // Поле Пароля
                    TrackademyTextField(
                        value = state.passwordInput,
                        onValueChange = {
                            component.handleIntent(AuthIntent.UpdateLoginFields(state.loginInput, it, state.selectedOrgId))
                        },
                        label = "Пароль",
                        placeholder = "••••••••",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    OrganizationDropdown(
                        organizations = state.organizations,
                        selectedOrgId = state.selectedOrgId,
                        onOrgSelected = { id ->
                            component.handleIntent(AuthIntent.UpdateLoginFields(state.loginInput, state.passwordInput, id))
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Кнопка Входа
                    Button(
                        onClick = { component.handleIntent(AuthIntent.LoginClicked) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Войти в систему", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }


            Spacer(Modifier.height(24.dp))
            TextButton(onClick = { component.handleIntent(AuthIntent.ToggleAuthMode) }) {
                Text("Нет аккаунта? Создать аккаунт", color = AccentBlue)
            }


            state.error?.let {
                Spacer(Modifier.height(16.dp))
                Text(text = it, color = Color.Red, textAlign = TextAlign.Center)
            }
        }
    }
}
