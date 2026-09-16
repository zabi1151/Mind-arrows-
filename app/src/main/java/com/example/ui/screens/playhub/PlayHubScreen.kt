package com.example.ui.screens.playhub

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.data.model.LevelProgressEntity
import com.example.data.model.UserStatsEntity
import com.example.game.levels.LevelDatabase
import com.example.game.model.Difficulty
import com.example.ui.components.AppHeader
import com.example.ui.components.AppNavTab
import com.example.ui.components.BottomNavBar
import com.example.ui.components.StarRatingRow
import com.example.ui.theme.AmberStar
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.Secondary
import com.example.ui.theme.Tertiary

@Composable
fun PlayHubScreen(
    userStats: UserStatsEntity,
    allProgress: List<LevelProgressEntity>,
    activeLevelId: Int,
    onLevelSelected: (Int) -> Unit,
    onPlayActiveClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNavTabSelected: (AppNavTab) -> Unit,
    onToggleDarkTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentPlayableLevel = remember(allProgress, activeLevelId) {
        allProgress.firstOrNull { it.isUnlocked && !it.isCompleted }?.levelId
            ?: allProgress.filter { it.isUnlocked }.maxOfOrNull { it.levelId }
            ?: activeLevelId
    }

    var selectedDifficulty by remember { mutableStateOf(Difficulty.fromLevel(currentPlayableLevel)) }

    val progressMap = remember(allProgress) {
        allProgress.associateBy { it.levelId }
    }

    val levelsForTab = remember(selectedDifficulty) {
        LevelDatabase.getLevelsForDifficulty(selectedDifficulty)
    }

    // Sector stats
    val clearedInDifficulty = levelsForTab.count { progressMap[it.id]?.isCompleted == true }
    val totalInDifficulty = levelsForTab.size

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Header
            AppHeader(
                title = "Play Hub",
                showBackButton = false,
                coins = userStats.coins,
                energy = 45,
                onProfileClick = onProfileClick,
                darkTheme = userStats.darkThemeEnabled,
                onToggleDarkTheme = onToggleDarkTheme,
                onSettingsClick = onSettingsClick
            )

            // 2. Main Scrollable Content
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("play_hub_grid"),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hero Banner: Active Sector Stage Showcase
                item(span = { GridItemSpan(4) }) {
                    ActiveStageHeroCard(
                        activeLevelId = currentPlayableLevel,
                        clearedInDifficulty = clearedInDifficulty,
                        totalInDifficulty = totalInDifficulty,
                        onPlayClick = onPlayActiveClick
                    )
                }

                // Star Vault & Economy Bar
                item(span = { GridItemSpan(4) }) {
                    EconomySummaryBar(userStats = userStats)
                }

                // Difficulty Tabs
                item(span = { GridItemSpan(4) }) {
                    DifficultyTabsStrip(
                        selectedDifficulty = selectedDifficulty,
                        onSelect = { selectedDifficulty = it }
                    )
                }

                // Level Cards (1..50 for Easy, etc.)
                items(levelsForTab) { level ->
                    val progress = progressMap[level.id]
                    val isUnlocked = progress?.isUnlocked ?: (level.id <= 1)
                    val isCompleted = progress?.isCompleted ?: false
                    val isActive = level.id == currentPlayableLevel
                    val stars = progress?.stars ?: 0

                    LevelGridItem(
                        levelId = level.id,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        isActive = isActive,
                        stars = stars,
                        onClick = {
                            if (isUnlocked) {
                                onLevelSelected(level.id)
                            }
                        }
                    )
                }

                // AdMob Banner in PlayHub
                item(span = { GridItemSpan(4) }) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AdBannerView()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Bottom Navigation Bar
            BottomNavBar(
                currentTab = AppNavTab.PLAY,
                onTabSelected = onNavTabSelected
            )
        }
    }
}

@Composable
private fun ActiveStageHeroCard(
    activeLevelId: Int,
    clearedInDifficulty: Int,
    totalInDifficulty: Int,
    onPlayClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_stage_hero_card"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TARGET SECTOR STAGE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Primary
                    )
                    Text(
                        text = "Stage $activeLevelId: Vortex Vector",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sector Progress",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$clearedInDifficulty / $totalInDifficulty Cleared (${(clearedInDifficulty * 100) / totalInDifficulty}%)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { clearedInDifficulty.toFloat() / totalInDifficulty },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("play_active_matrix_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY ACTIVE MATRIX",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun EconomySummaryBar(userStats: UserStatsEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = AmberStar, modifier = Modifier.size(18.dp))
                Text(
                    text = "Vault: ${userStats.totalStars} ★",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Tokens: ${userStats.tokens}",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Tier: Mastermind",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Primary
            )
        }
    }
}

@Composable
private fun DifficultyTabsStrip(
    selectedDifficulty: Difficulty,
    onSelect: (Difficulty) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedDifficulty.ordinal,
        edgePadding = 0.dp,
        containerColor = Color.Transparent,
        divider = {}
    ) {
        Difficulty.values().forEach { diff ->
            val isSelected = diff == selectedDifficulty
            Tab(
                selected = isSelected,
                onClick = { onSelect(diff) },
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Primary else MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                Text(
                    text = diff.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LevelGridItem(
    levelId: Int,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    isActive: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    val containerColor = when {
        isActive -> MaterialTheme.colorScheme.surfaceContainerLowest
        isCompleted -> MaterialTheme.colorScheme.surfaceContainerLowest
        isUnlocked -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)
    }

    val borderModifier = if (isActive) {
        Modifier.border(2.dp, Primary, RoundedCornerShape(16.dp))
    } else Modifier

    Surface(
        modifier = Modifier
            .size(76.dp)
            .then(borderModifier)
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = isUnlocked, onClick = onClick)
            .testTag("level_cell_$levelId"),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = if (isActive || isCompleted) 3.dp else 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "$levelId",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold
                    ),
                    color = if (isActive) Primary else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (isCompleted) {
                    StarRatingRow(stars = stars)
                } else if (isActive) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Primary)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "PLAY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
