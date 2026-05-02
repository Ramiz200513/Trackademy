package org.example.project.feature.student.perfomance.component
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainPerformanceBanner(avgGrade: Double, attendance: Int, assignments: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF10B981))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Иконка в белом кружке или просто белая
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Моя статистика успеваемости",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Обзор моей учебы",
                            color = Color.White.copy(0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2937))
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BannerMetric(avgGrade.toString(), "Средний балл", Color(0xFF10B981))
                // Тонкий вертикальный разделитель
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(0.1f)))
                BannerMetric("$attendance%", "Посещаемость", Color(0xFF3B82F6))
                Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(0.1f)))
                BannerMetric(assignments.toString(), "Задания", Color(0xFFF59E0B))
            }
        }
    }
}

@Composable
private fun BannerMetric(value: String, label: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = accentColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}