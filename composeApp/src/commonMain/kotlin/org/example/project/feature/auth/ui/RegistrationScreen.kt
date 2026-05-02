package org.example.project.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.ui.theme.*
import org.example.project.feature.auth.AuthComponent
import org.example.project.feature.auth.AuthIntent
import org.example.project.feature.component.TrackademyTextField
import org.jetbrains.compose.resources.painterResource
import trackademy.composeapp.generated.resources.Res
import trackademy.composeapp.generated.resources.compose_multiplatform // Используем пока стандартный, если ваш не сгенерировался

@Composable
fun RegistrationScreen(component: AuthComponent) {
    val state by component.state.collectAsState()
    val draft = state.registrationDraft

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundDark, Color.Black)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                 IconButton(onClick = { /* onBack */ }) {
                     Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextWhite)
                 }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.compose_multiplatform),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AccentBlue
                )
            }

            Spacer(Modifier.height(24.dp))
            
            Text(
                "Регистрация",
                color = TextWhite,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Заполните данные для создания профиля",
                color = TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(32.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    TrackademyTextField(
                        value = draft.fullName,
                        onValueChange = { component.handleIntent(AuthIntent.UpdateRegistrationField(draft.copy(fullName = it))) },
                        label = "Полное имя",
                        placeholder = "Иван Иванов"
                    )

                    TrackademyTextField(
                        value = draft.login,
                        onValueChange = { component.handleIntent(AuthIntent.UpdateRegistrationField(draft.copy(login = it))) },
                        label = "Логин",
                        placeholder = "Придумайте логин"
                    )

                    TrackademyTextField(
                        value = draft.password,
                        onValueChange = { component.handleIntent(AuthIntent.UpdateRegistrationField(draft.copy(password = it))) },
                        label = "Пароль",
                        placeholder = "••••••••",
                        visualTransformation = PasswordVisualTransformation()
                    )

                    TrackademyTextField(
                        value = draft.phone,
                        onValueChange = { component.handleIntent(AuthIntent.UpdateRegistrationField(draft.copy(phone = it))) },
                        label = "Телефон",
                        placeholder = "+7 (XXX) XXX XX-XX",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )

                    TrackademyTextField(
                        value = draft.activationKey,
                        onValueChange = { input ->
                            if (input.length <= 11) {
                                val formatted = formatActivationKey(input.uppercase())
                                component.handleIntent(AuthIntent.UpdateRegistrationField(draft.copy(activationKey = formatted)))
                            }
                        },
                        label = "Ключ активации",
                        placeholder = "XXX-XXX-XXX"
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = { component.handleIntent(AuthIntent.RegisterClicked) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            disabledContainerColor = AccentBlue.copy(alpha = 0.5f)
                        ),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextWhite,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.AccountCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Зарегистрироваться", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    
                    if (state.error != null) {
                        Text(
                            text = state.error!!,
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = { component.handleIntent(AuthIntent.ToggleAuthMode) }) {
                Text("Уже есть аккаунт? Войти", color = TextGray)
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

fun formatActivationKey(input: String): String {
    val cleaned = input.replace("-", "").filter { it.isLetterOrDigit() }

    val sb = StringBuilder()
    for (i in cleaned.indices) {
        sb.append(cleaned[i])
        if ((i == 2 || i == 5) && i != cleaned.lastIndex) {
            sb.append("-")
        }
    }
    return sb.toString()
}
