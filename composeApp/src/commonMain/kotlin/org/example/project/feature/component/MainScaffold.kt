package org.example.project.feature.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.ui.theme.*
import org.example.project.feature.student.perfomance.ui.PerformanceScreen
import org.example.project.feature.performance.PerformanceComponent

@Composable
fun MainScaffold(component: PerformanceComponent) {
    Scaffold(
        containerColor = BackgroundDark,
        bottomBar = {
            NavigationBar(
                containerColor = BackgroundDark.copy(0.95f),
                tonalElevation = 0.dp,
                modifier = Modifier.height(80.dp)
            ) {
                val items = listOf(
                    Triple("Главная", Icons.Default.Home, true),
                    Triple("График", Icons.Default.DateRange, false),
                    Triple("Профиль", Icons.Default.Person, false)
                )

                items.forEach { (label, icon, isSelected) ->
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { /* TODO */ },
                        icon = { Icon(icon, null, modifier = Modifier.size(24.dp)) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            PerformanceScreen(component)
        }
    }
}