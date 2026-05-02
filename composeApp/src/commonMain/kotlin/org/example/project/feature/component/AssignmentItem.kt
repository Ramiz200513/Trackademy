package org.example.project.feature.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.core.ui.theme.*
import org.example.project.domain.models.Assignment
@Composable
fun AssignmentItem(assignment: Assignment) {
    val statusColor = if (assignment.isOverdue) Color(0xFFEF4444) else AccentBlue

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (assignment.isOverdue) Color(0xFF2D1616) else CardBackground
        ),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(statusColor, CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (assignment.isOverdue) "ПРОСРОЧЕНО" else assignment.status,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(assignment.description, color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text("${assignment.subjectName} • ${assignment.groupName}", color = TextGray, fontSize = 12.sp)

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Срок: ${assignment.dueDate.take(10)}",
                    color = TextWhite.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { /* Navigate */ },
                colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Открыть", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}