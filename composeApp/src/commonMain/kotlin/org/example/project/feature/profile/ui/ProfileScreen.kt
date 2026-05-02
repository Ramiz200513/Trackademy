package org.example.project.feature.profile.ui

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.remote.dto.MarketItemDto
import org.example.project.data.remote.dto.PurchaseHistoryDto
import org.example.project.feature.profile.MarketTab
import org.example.project.feature.profile.ProfileComponent
import org.example.project.core.ui.theme.*


@Composable
fun ProfileScreen(component: ProfileComponent) {
    val state by component.state.collectAsState()

    LaunchedEffect(state.passwordSuccess) {
        if (state.passwordSuccess != null) {
            kotlinx.coroutines.delay(3000)
            component.clearMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BgDark)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                // ── Header ────────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(Color(0xFF3D1F8C), Purple)))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text(
                            "Профиль",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(20.dp))

                        // Avatar + name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = state.profile?.fullName
                                    ?.split(" ")
                                    ?.filter { it.isNotBlank() }
                                    ?.mapNotNull { it.firstOrNull()?.toString()?.uppercase() }
                                    ?.take(2)
                                    ?.joinToString("") ?: "?"

                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    state.profile?.fullName ?: "Загрузка...",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    state.profile?.login?.let { "@$it" } ?: "",
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Loading
            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PurpleLight)
                    }
                }
                return@LazyColumn
            }

            state.profile?.let { profile ->

                // ── Info cards row ─────────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        InfoCard(
                            label = "Роль",
                            value = profile.roleName,
                            icon = "👤",
                            modifier = Modifier.weight(1f)
                        )
                        InfoCard(
                            label = "Организация",
                            value = profile.organizationName ?: "—",
                            icon = "🏫",
                            modifier = Modifier.weight(1f)
                        )
                        state.coinBalance?.let { coin ->
                            InfoCard(
                                label = "Монеты",
                                value = "${coin.balance} 🪙",
                                icon = "💰",
                                accent = Gold,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── Details section ───────────────────────────────────────────
                item {
                    SectionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        SectionTitle("Личная информация")
                        Spacer(Modifier.height(12.dp))
                        ProfileField(label = "Полное имя", value = profile.fullName, icon = "👤")
                        ProfileField(label = "Логин", value = "@${profile.login}", icon = "🔑")
                        ProfileField(label = "Роль", value = profile.roleName, icon = "🎓")
                        ProfileField(label = "Организация", value = profile.organizationName ?: "—", icon = "🏫", isLast = true)
                    }
                }

                // ── Action buttons ─────────────────────────────────────────────
                item {
                    Spacer(Modifier.height(16.dp))
                    SectionCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        SectionTitle("Действия")
                        Spacer(Modifier.height(12.dp))

                        // Market button
                        ActionButton(
                            label = "🛒  Маркет",
                            description = "Потратить монеты на призы",
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF065F46), Color(0xFF059669))),
                            onClick = { component.openMarket() }
                        )
                        Spacer(Modifier.height(10.dp))

                        // Change password button
                        ActionButton(
                            label = "🔒  Сменить пароль",
                            description = "Обновить пароль аккаунта",
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))),
                            onClick = { component.openChangePassword() }
                        )
                        Spacer(Modifier.height(10.dp))

                        // Logout button
                        ActionButton(
                            label = "🚪  Выйти",
                            description = "Выйти из аккаунта",
                            gradient = Brush.horizontalGradient(listOf(Color(0xFF7F1D1D), Color(0xFFEF4444))),
                            onClick = { component.logout() }
                        )
                    }
                }
            }

            // Error state
            if (state.error != null) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📡", fontSize = 40.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(state.error!!, color = Red, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // ── Success toast ─────────────────────────────────────────────────────
        AnimatedVisibility(
            visible = state.passwordSuccess != null,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .border(1.dp, Green.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text("✅ ${state.passwordSuccess}", color = TextWhite, fontSize = 13.sp)
            }
        }

        AnimatedVisibility(
            visible = state.showMarket,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            MarketSheet(
                items = state.marketItems,
                isLoading = state.isMarketLoading,
                error = state.marketError,
                coinBalance = state.coinBalance?.balance ?: 0,
                purchaseHistory = state.purchaseHistory,
                isPurchasing = state.isPurchasing,
                purchaseSuccess = state.purchaseSuccess,
                purchaseError = state.purchaseError,
                activeTab = state.marketTab,
                onTabChange = { component.setMarketTab(it) },
                onPurchase = { component.purchaseItem(it) },
                onClose = { component.closeMarket() }
            )
        }

        // ── Change password dialog ────────────────────────────────────────────
        if (state.showPasswordDialog) {
            ChangePasswordDialog(
                isLoading = state.isPasswordLoading,
                error = state.passwordError,
                onConfirm = { current, new, confirm ->
                    component.changePassword(current, new, confirm)
                },
                onDismiss = { component.closeChangePassword() }
            )
        }
    }
}

// ── Reusable components ───────────────────────────────────────────────────────

@Composable
private fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, Color(0xFF2A2A38), RoundedCornerShape(16.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    icon: String,
    accent: Color = PurpleLight,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            color = accent,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(label, color = TextMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    icon: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(28.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = TextMuted, fontSize = 11.sp)
            Text(value, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
    if (!isLast) {
        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFF1A1A28))
    }
}

@Composable
private fun ActionButton(
    label: String,
    description: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(gradient)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = Color.White.copy(alpha = 0.65f), fontSize = 11.sp)
        }
    }
}



@Composable
private fun MarketSheet(
    items: List<MarketItemDto>,
    isLoading: Boolean,
    error: String?,
    coinBalance: Int,
    purchaseHistory: List<PurchaseHistoryDto>,
    isPurchasing: Boolean,
    purchaseSuccess: String?,
    purchaseError: String?,
    activeTab: MarketTab,
    onTabChange: (MarketTab) -> Unit,
    onPurchase: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(SurfaceDark)
                .clickable(enabled = false) {}
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Color(0xFF065F46), Color(0xFF059669))))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🛒  Маркет", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Баланс: $coinBalance 🪙", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // ── Tabs ──────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MarketTabButton(
                    label = "🏪  Магазин",
                    isActive = activeTab == MarketTab.SHOP,
                    onClick = { onTabChange(MarketTab.SHOP) },
                    modifier = Modifier.weight(1f)
                )
                MarketTabButton(
                    label = "📦  Мои покупки",
                    isActive = activeTab == MarketTab.HISTORY,
                    onClick = { onTabChange(MarketTab.HISTORY) },
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Purchase feedback ─────────────────────────────────────────────
            AnimatedVisibility(visible = purchaseSuccess != null || purchaseError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (purchaseSuccess != null) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.15f))
                        .border(1.dp, if (purchaseSuccess != null) Green.copy(alpha = 0.4f) else Red.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        purchaseSuccess ?: purchaseError ?: "",
                        color = if (purchaseSuccess != null) Green else Red,
                        fontSize = 13.sp
                    )
                }
            }

            // ── Content ───────────────────────────────────────────────────────
            when (activeTab) {
                MarketTab.SHOP -> ShopContent(
                    items = items,
                    isLoading = isLoading,
                    error = error,
                    coinBalance = coinBalance,
                    isPurchasing = isPurchasing,
                    onPurchase = onPurchase
                )
                MarketTab.HISTORY -> HistoryContent(history = purchaseHistory)
            }
        }
    }
}
@Composable
private fun HistoryContent(history: List<PurchaseHistoryDto>) {
    if (history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📦", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Покупок пока нет", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Купите что-нибудь в магазине", color = TextMuted, fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(history) { purchase ->
                PurchaseHistoryCard(purchase)
            }
        }
    }
}
@Composable
private fun PurchaseHistoryCard(purchase: PurchaseHistoryDto) {
    val statusColor = when (purchase.status) {
        1 -> Gold        // В обработке
        2 -> Green       // Выдан
        3 -> Red         // Отклонён
        else -> TextMuted
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, statusColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(statusColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (purchase.status) { 2 -> "✅"; 3 -> "❌"; else -> "⏳" },
                fontSize = 20.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(purchase.itemName, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(purchase.statusName ?: "", color = statusColor, fontSize = 11.sp)
        }
        Text("${purchase.pricePaid} 🪙", color = Gold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
@Composable
private fun MarketTabButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isActive) Green.copy(alpha = 0.2f) else Color(0xFF1A1A28))
            .border(1.dp, if (isActive) Green.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (isActive) Green else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ShopContent(
    items: List<MarketItemDto>,
    isLoading: Boolean,
    error: String?,
    coinBalance: Int,
    isPurchasing: Boolean,
    onPurchase: (String) -> Unit
) {
    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Green)
        }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("😕", fontSize = 40.sp)
                Spacer(Modifier.height(12.dp))
                Text(error, color = Red, fontSize = 15.sp)
            }
        }
        items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🏪", fontSize = 48.sp)
                Spacer(Modifier.height(12.dp))
                Text("Товаров пока нет", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("Скоро здесь появятся призы", color = TextMuted, fontSize = 13.sp)
            }
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items) { item ->
                MarketItemCard(
                    item = item,
                    coinBalance = coinBalance,
                    isPurchasing = isPurchasing,
                    onPurchase = { onPurchase(item.id) }
                )
            }
        }
    }
}

@Composable
private fun MarketItemCard(
    item: MarketItemDto,
    coinBalance: Int,
    isPurchasing: Boolean,
    onPurchase: () -> Unit
) {
    val canAfford = coinBalance >= item.price
    val borderColor = if (canAfford) Green.copy(alpha = 0.4f) else Red.copy(alpha = 0.25f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (canAfford) Green.copy(alpha = 0.1f) else Color(0xFF1A1A28)),
            contentAlignment = Alignment.Center
        ) {
            Text("🎁", fontSize = 28.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            item.name,
            color = TextWhite,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (!item.description.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(item.description, color = TextMuted, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (canAfford) Green.copy(alpha = 0.15f) else Red.copy(alpha = 0.1f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text("${item.price} 🪙", color = if (canAfford) Green else Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        // ── Buy button ───────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (canAfford) Brush.horizontalGradient(listOf(Color(0xFF065F46), Color(0xFF059669)))
                    else Brush.horizontalGradient(listOf(Color(0xFF2A1A1A), Color(0xFF3A1A1A)))
                )
                .clickable(enabled = canAfford && !isPurchasing) { onPurchase() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isPurchasing) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    if (canAfford) "Купить" else "Недостаточно",
                    color = if (canAfford) Color.White else Red.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    error: String?,
    onConfirm: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        shape = RoundedCornerShape(16.dp),
        title = {
            Text("Смена пароля", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PasswordField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = "Текущий пароль",
                    visible = showCurrent,
                    onToggleVisible = { showCurrent = !showCurrent }
                )
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "Новый пароль",
                    visible = showNew,
                    onToggleVisible = { showNew = !showNew }
                )
                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Подтвердите пароль",
                    visible = showNew,
                    onToggleVisible = { showNew = !showNew }
                )
                if (error != null) {
                    Text(error, color = Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentPassword, newPassword, confirmPassword) },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleLight),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Сохранить", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextMuted)
            }
        }
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggleVisible: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            TextButton(onClick = onToggleVisible, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text(if (visible) "Скрыть" else "Показать", color = PurpleLight, fontSize = 11.sp)
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceDark,
            unfocusedContainerColor = SurfaceDark,
            focusedBorderColor = PurpleLight,
            unfocusedBorderColor = Color(0xFF2A2A38),
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedLabelColor = PurpleLight,
            unfocusedLabelColor = TextMuted
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    )
}