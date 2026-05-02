package org.example.project.feature.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.project.core.ui.theme.*
import org.example.project.domain.models.Organization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationDropdown(
    organizations: List<Organization>,
    selectedOrgId: String,
    onOrgSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOrgName = organizations.find { it.id == selectedOrgId }?.name ?: "Выберите организацию"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Организация *",
            color = TextWhite,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = selectedOrgName,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextWhite
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = InputBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    disabledTextColor = TextWhite,
                ),
                enabled = false 
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(CardBackground)
            ) {
                organizations.forEach { org ->
                    DropdownMenuItem(
                        text = { 
                            Text(org.name, color = TextWhite) 
                        },
                        onClick = {
                            onOrgSelected(org.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
