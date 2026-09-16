package com.example.ui.screens.shop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.data.model.UserStatsEntity
import com.example.game.model.ArrowSkin
import com.example.game.model.ArrowSkinCatalog
import com.example.ui.components.AppHeader
import com.example.ui.components.AppNavTab
import com.example.ui.components.BottomNavBar
import com.example.ui.theme.AmberStar
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.Tertiary
import com.example.ui.theme.TertiaryFixed

@Composable
fun ShopScreen(
    userStats: UserStatsEntity,
    onBuySkin: (String, Int) -> Unit,
    onEquipSkin: (String) -> Unit,
    onWatchAdForCoins: () -> Unit,
    onNavTabSelected: (AppNavTab) -> Unit,
    onProfileClick: () -> Unit,
    onToggleDarkTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val unlockedSkins = remember(userStats.unlockedArrowSkins) {
        userStats.unlockedArrowSkins.split(",").map { it.trim() }.toSet()
    }

    var selectedSkin by remember {
        mutableStateOf(ArrowSkinCatalog.getSkin(userStats.equippedArrowSkin))
    }

    val isSelectedOwned = selectedSkin.id in unlockedSkins
    val isSelectedEquipped = userStats.equippedArrowSkin == selectedSkin.id

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            AppHeader(
                title = "Arrow Arsenal",
                showBackButton = false,
                coins = userStats.coins,
                energy = 45,
                onProfileClick = onProfileClick,
                darkTheme = userStats.darkThemeEnabled,
                onToggleDarkTheme = onToggleDarkTheme,
                onSettingsClick = onSettingsClick
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Hero Preview of Selected Arrow Skin
                item(span = { GridItemSpan(2) }) {
                    HeroSkinPreview(
                        skin = selectedSkin,
                        isOwned = isSelectedOwned,
                        isEquipped = isSelectedEquipped,
                        userCoins = userStats.coins,
                        onEquip = { onEquipSkin(selectedSkin.id) },
                        onBuy = { onBuySkin(selectedSkin.id, selectedSkin.price) }
                    )
                }

                // 2. Watch Ad for Free Coins Banner
                item(span = { GridItemSpan(2) }) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onWatchAdForCoins)
                            .testTag("watch_ad_coins_button"),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            AmberStar.copy(alpha = 0.12f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(AmberStar.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartDisplay,
                                        contentDescription = "Ad",
                                        tint = AmberStar,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Need Coins? Watch Test Ad",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Earn +100 Coins instantly",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AmberStar
                            ) {
                                Text(
                                    text = "+100 🪙",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Section Title
                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = "Available Arrow Styles",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 4. Skin Cards Grid
                items(ArrowSkinCatalog.allSkins) { skin ->
                    val isOwned = skin.id in unlockedSkins
                    val isEquipped = userStats.equippedArrowSkin == skin.id
                    val isSelected = selectedSkin.id == skin.id

                    ArrowSkinCard(
                        skin = skin,
                        isOwned = isOwned,
                        isEquipped = isEquipped,
                        isSelected = isSelected,
                        onClick = { selectedSkin = skin }
                    )
                }

                // 5. AdMob Banner at bottom
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(6.dp))
                    AdBannerView()
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Bottom Nav
            BottomNavBar(
                currentTab = AppNavTab.SHOP,
                onTabSelected = onNavTabSelected
            )
        }
    }
}

@Composable
private fun HeroSkinPreview(
    skin: ArrowSkin,
    isOwned: Boolean,
    isEquipped: Boolean,
    userCoins: Int,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroPulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skin_hero_preview"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SELECTED SKIN PREVIEW",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Big Interactive Arrow Representation
            Box(
                modifier = Modifier
                    .size(width = 190.dp, height = 56.dp)
                    .scale(pulseScale)
                    .shadow(12.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.horizontalGradient(skin.gradientColors))
                    .border(
                        width = 2.dp,
                        color = skin.accentColor,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tail
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(skin.gradientColors.first())
                        )
                    }

                    // Connecting stem dots
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f))
                            )
                        }
                    }

                    // Head
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .shadow(4.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = skin.iconVector,
                            contentDescription = "Skin Head",
                            tint = skin.gradientColors.first(),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = skin.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = skin.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button: Equipped / Equip / Buy
            when {
                isEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Tertiary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Tertiary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "EQUIPPED IN GAME",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Tertiary
                            )
                        }
                    }
                }
                isOwned -> {
                    Button(
                        onClick = onEquip,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        modifier = Modifier.testTag("equip_skin_button")
                    ) {
                        Text(
                            text = "EQUIP ARROW",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
                else -> {
                    val canAfford = userCoins >= skin.price
                    Button(
                        onClick = onBuy,
                        enabled = canAfford,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberStar,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        modifier = Modifier.testTag("buy_skin_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Toll,
                                contentDescription = "Coins",
                                tint = if (canAfford) Color.Black else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "UNLOCK FOR ${skin.price} COINS",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (canAfford) Color.Black else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    if (!canAfford) {
                        Text(
                            text = "You need ${skin.price - userCoins} more coins",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArrowSkinCard(
    skin: ArrowSkin,
    isOwned: Boolean,
    isEquipped: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("skin_card_${skin.id}"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = if (isSelected) 6.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.5.dp else 0.5.dp,
            color = if (isSelected) Primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Miniature Skin Arrow Representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.horizontalGradient(skin.gradientColors))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.85f))
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = skin.iconVector,
                            contentDescription = null,
                            tint = skin.gradientColors.first(),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = skin.name,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Status Tag
            when {
                isEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Tertiary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "EQUIPPED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Tertiary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                isOwned -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = "OWNED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                else -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AmberStar.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Toll,
                                contentDescription = null,
                                tint = AmberStar,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${skin.price}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AmberStar
                            )
                        }
                    }
                }
            }
        }
    }
}
