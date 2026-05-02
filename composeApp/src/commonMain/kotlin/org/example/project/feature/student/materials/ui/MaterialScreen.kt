package org.example.project.feature.student.materials.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.models.Material
import org.example.project.feature.student.materials.MaterialComponent
import org.example.project.core.ui.theme.*
data class GroupChip(val id: String, val name: String)




@Composable
fun MaterialScreen(component: MaterialComponent) {
    val state by component.state.collectAsState()

    LaunchedEffect(state.message) {
        if (state.message != null) {
            kotlinx.coroutines.delay(3000)
            component.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF3D1F8C), Purple)))
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column {
                    Text(
                        "Учебные материалы",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${state.materials.size} материалов",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }
            }

            // ── Search bar ────────────────────────────────────────────────────
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { component.onSearchChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Поиск по названию...", color = TextMuted, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { component.onSearchChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBg,
                    unfocusedContainerColor = InputBg,
                    focusedBorderColor = Purple,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // ── Group filter chips ─────────────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                item {
                    GroupFilterChip(
                        label = "Все группы",
                        selected = state.selectedGroupId == null,
                        onClick = { component.onGroupSelected(null) }
                    )
                }
                items(state.groups) { group ->
                    GroupFilterChip(
                        label = group.name,
                        selected = state.selectedGroupId == group.id,
                        onClick = { component.onGroupSelected(group.id) }
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PurpleLight)
                    }
                }
                state.error != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📡", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(state.error!!, color = Red, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                state.materials.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📂", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text("Материалов не найдено", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text("Попробуйте изменить фильтры", color = TextMuted, fontSize = 13.sp)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.materials, key = { it.id }) { material ->
                            MaterialCard(
                                material = material,
                                isDownloading = state.downloadingId == material.id,
                                onPreview = { component.previewMaterial(material) },
                                onDownload = { component.downloadMaterial(material) }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }

        // ── Toast message ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.message != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        ) {
            state.message?.let { msg ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .border(1.dp, if (msg.startsWith("✅")) Green.copy(alpha = 0.4f) else Red.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(msg, color = TextWhite, fontSize = 13.sp)
                }
            }
        }

        // ── Image Preview overlay ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.previewMaterial != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            state.previewMaterial?.let { material ->
                ImagePreviewOverlay(
                    material = material,
                    imageBytes = state.previewBytes,
                    onClose = { component.closePreview() },
                    onDownload = { component.downloadMaterial(material) }
                )
            }
        }
    }
}

@Composable
private fun GroupFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Purple else CardDark)
            .border(
                1.dp,
                if (selected) Purple else Color(0xFF2A2A38),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else TextMuted,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun MaterialCard(
    material: Material,
    isDownloading: Boolean,
    onPreview: () -> Unit,
    onDownload: () -> Unit
) {
    val accentColor = when {
        material.isImage    -> Color(0xFF10B981)
        material.isPdf      -> Color(0xFFEF4444)
        material.isDocument -> Color(0xFF3B82F6)
        else                -> Purple
    }

    val fileIcon = when {
        material.isImage    -> "🖼️"
        material.isPdf      -> "📕"
        material.isDocument -> "📝"
        else                -> "📎"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // File type icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(fileIcon, fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            // Title + description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    material.title,
                    color = TextWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!material.description.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        material.description,
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!material.groupName.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Purple.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(material.groupName, color = PurpleLight, fontSize = 10.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "${material.fileSizeKb} KB  •  ${material.fileExtension.uppercase()}",
                        color = TextDim,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Action buttons
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = PurpleLight,
                    strokeWidth = 2.dp
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Preview button
                    ActionIconButton(
                        emoji = "👁",
                        tint = Blue,
                        onClick = onPreview
                    )
                    // Download button
                    ActionIconButton(
                        emoji = "⬇",
                        tint = Green,
                        onClick = onDownload
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionIconButton(emoji: String, tint: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.12f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = 14.sp)
    }
}

@Composable
private fun ImagePreviewOverlay(
    material: Material,
    imageBytes: ByteArray?,
    onClose: () -> Unit,
    onDownload: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .clickable(enabled = false) {}
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    material.title,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = TextMuted)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Image preview placeholder (real image rendering requires platform-specific impl)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF111118)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🖼️", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(material.originalFileName, color = TextMuted, fontSize = 12.sp)
                    Text("${material.fileSizeKb} KB", color = TextDim, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onDownload,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleLight),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("⬇  Скачать файл", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}