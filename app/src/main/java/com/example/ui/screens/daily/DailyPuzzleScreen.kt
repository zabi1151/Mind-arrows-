package com.example.ui.screens.daily

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.data.model.UserStatsEntity
import com.example.ui.components.AppHeader
import com.example.ui.components.AppNavTab
import com.example.ui.components.BottomNavBar
import com.example.ui.theme.AmberStar
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.Tertiary

data class DailyMissionItem(
    val id: String,
    val title: String,
    val description: String,
    val progress: Int,
    val target: Int,
    val rewardCoins: Int
)

@Composable
fun DailyPuzzleScreen(
    userStats: UserStatsEntity,
    onPlayDailyClick: () -> Unit,
    onNavTabSelected: (AppNavTab) -> Unit,
    onProfileClick: () -> Unit,
    onClaimMission: (String, Int) -> Unit = { _, _ -> },
    onToggleDarkTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val claimedMissions = remember(userStats.dailyMissionsClaimed) {
        userStats.dailyMissionsClaimed.split(",").map { it.trim() }.toSet()
    }

    // Dynamic daily missions tracking real gameplay
    val missions = listOf(
        DailyMissionItem(
            id = "mission_clear_1",
            title = "First Flight",
            description = "Clear your first arrow puzzle",
            progress = minOf(userStats.totalLevelsCleared, 1),
            target = 1,
            rewardCoins = 50
        ),
        DailyMissionItem(
            id = "mission_moves_10",
            title = "Vector Velocity",
            description = "Disentangle 10 arrows across levels",
            progress = minOf(userStats.totalMovesMade, 10),
            target = 10,
            rewardCoins = 75
        ),
        DailyMissionItem(
            id = "mission_no_hints",
            title = "Pure Intuition",
            description = "Complete a level without using hints",
            progress = if (userStats.totalLevelsCleared >= 1 && userStats.totalHintsUsed == 0) 1 else 0,
            target = 1,
            rewardCoins = 100
        )
    )

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                title = "Daily Challenge",
                showBackButton = false,
                coins = userStats.coins,
                energy = 45,
                onProfileClick = onProfileClick,
                darkTheme = userStats.darkThemeEnabled,
                onToggleDarkTheme = onToggleDarkTheme,
                onSettingsClick = onSettingsClick
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Streak Banner (starts from 0)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Streak",
                                tint = if (userStats.dailyStreak > 0) Color(0xFFEF4444) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (userStats.dailyStreak > 0) "${userStats.dailyStreak}-Day Brain Streak!" else "0-Day Streak • Start Today!",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (userStats.dailyStreak > 0) "Keep solving daily to earn multiplier bonuses!" else "Complete today's challenge to ignite your streak counter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Daily Missions Card (Scratch & Functional)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Daily Missions (0/3 Completed)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        missions.forEach { mission ->
                            val isClaimed = mission.id in claimedMissions
                            val isReadyToClaim = mission.progress >= mission.target && !isClaimed

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = mission.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = mission.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            LinearProgressIndicator(
                                                progress = { mission.progress.toFloat() / mission.target.toFloat() },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = if (mission.progress >= mission.target) Tertiary else Primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            )
                                            Text(
                                                text = "${mission.progress}/${mission.target}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    when {
                                        isClaimed -> {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Tertiary.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    text = "CLAIMED",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = Tertiary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                        isReadyToClaim -> {
                                            Button(
                                                onClick = { onClaimMission(mission.id, mission.rewardCoins) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = AmberStar),
                                                modifier = Modifier.testTag("claim_mission_${mission.id}")
                                            ) {
                                                Text(
                                                    text = "CLAIM +${mission.rewardCoins} 🪙",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = Color.Black
                                                )
                                            }
                                        }
                                        else -> {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHigh
                                            ) {
                                                Text(
                                                    text = "+${mission.rewardCoins} 🪙",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    ),
                                                    color = AmberStar,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Today's Special Daily Matrix
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S SPECIAL MATRIX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Primary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Toll, contentDescription = null, tint = AmberStar, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "+200 Coins",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Quantum Chrono Matrix",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Difficulty: Adaptive • 7x7 Grid • High-Complexity Interlocking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = onPlayDailyClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("play_daily_puzzle_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "SOLVE TODAY'S PUZZLE", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Weekly Calendar Strip (Calculated dynamically)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "This Week's Record",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val daysList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        val activeStreakDays = minOf(userStats.dailyStreak, 7)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            daysList.forEachIndexed { index, d ->
                                val isDone = index < activeStreakDays
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(if (isDone) Tertiary else MaterialTheme.colorScheme.surfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "•",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                    Text(
                                        text = d,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // AdMob Banner View
                AdBannerView()
            }

            BottomNavBar(
                currentTab = AppNavTab.DAILY,
                onTabSelected = onNavTabSelected
            )
        }
    }
}
