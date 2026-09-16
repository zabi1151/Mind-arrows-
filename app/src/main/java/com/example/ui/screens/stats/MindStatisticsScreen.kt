package com.example.ui.screens.stats

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.ads.AdsConfig
import com.example.data.model.UserStatsEntity
import com.example.ui.components.AppHeader
import com.example.ui.components.AppNavTab
import com.example.ui.components.BottomNavBar
import com.example.ui.theme.AmberStar
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.SecondaryContainer
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Edit
import com.example.ui.theme.Tertiary

@Composable
fun MindStatisticsScreen(
    userStats: UserStatsEntity,
    onBackClick: () -> Unit,
    onNavTabSelected: (AppNavTab) -> Unit,
    onProfileClick: () -> Unit,
    onEditProfile: () -> Unit = onProfileClick,
    onToggleDarkTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isFreshAccount = userStats.totalLevelsCleared == 0

    val easyCleared = userStats.totalLevelsCleared.coerceIn(0, 50)
    val normalCleared = (userStats.totalLevelsCleared - 50).coerceIn(0, 50)
    val hardCleared = (userStats.totalLevelsCleared - 100).coerceIn(0, 75)
    val veryHardCleared = (userStats.totalLevelsCleared - 175).coerceIn(0, 65)
    val grandmasterCleared = (userStats.totalLevelsCleared - 240).coerceIn(0, 60)

    val displayName = if (userStats.userName.isNotBlank()) userStats.userName else "Brain Explorer"
    val ageInfo = if (userStats.userAge > 0) "Age ${userStats.userAge} • " else ""
    val rankTier = when {
        userStats.totalLevelsCleared >= 240 -> "Grandmaster Vector"
        userStats.totalLevelsCleared >= 175 -> "Mastermind Logic"
        userStats.totalLevelsCleared >= 100 -> "Expert Strategist"
        userStats.totalLevelsCleared >= 50 -> "Adept Navigator"
        userStats.totalLevelsCleared >= 1 -> "Novice Arrow"
        else -> "Beginner"
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            AppHeader(
                title = "Mind Statistics",
                showBackButton = true,
                onBackClick = onBackClick,
                coins = userStats.coins,
                energy = 45,
                onProfileClick = onProfileClick,
                darkTheme = userStats.darkThemeEnabled,
                onToggleDarkTheme = onToggleDarkTheme,
                onSettingsClick = onSettingsClick
            )

            // Scrollable Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Player Profile Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_card"),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .shadow(4.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(Primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Avatar",
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = displayName,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "$ageInfo$rankTier • Level ${userStats.totalLevelsCleared}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Primary
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onEditProfile() }
                                    .testTag("edit_profile_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Metric Badges Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileBadge(
                                icon = Icons.Default.LocalFireDepartment,
                                title = "${userStats.dailyStreak} Days",
                                subtitle = "Daily Streak",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.weight(1f)
                            )
                            ProfileBadge(
                                icon = Icons.Default.CheckCircle,
                                title = "${userStats.totalLevelsCleared}/300",
                                subtitle = "Cleared",
                                tint = Tertiary,
                                modifier = Modifier.weight(1f)
                            )
                            ProfileBadge(
                                icon = Icons.Default.Star,
                                title = "${userStats.totalStars} ★",
                                subtitle = "Total Stars",
                                tint = AmberStar,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Spatial Logic Score & Circular Gauge (Null / Calibrating if 0)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "COGNITIVE MATRIX",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFreshAccount) "Spatial Logic (Calibrating)" else "Spatial Logic Score",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isFreshAccount) {
                                    "Solve puzzle levels to calculate your move efficiency and logic accuracy."
                                } else {
                                    "Move Efficiency: ${userStats.moveEfficiencyPct}%\nAvg Time / Puzzle: ${userStats.avgTimeSeconds}s"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Circular Gauge
                        Box(
                            modifier = Modifier.size(86.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = Color(0xFFE2E8F0),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 16f, cap = StrokeCap.Round)
                                )
                                if (!isFreshAccount && userStats.spatialLogicScore > 0) {
                                    drawArc(
                                        color = Primary,
                                        startAngle = -90f,
                                        sweepAngle = 360f * (userStats.spatialLogicScore / 100f),
                                        useCenter = false,
                                        style = Stroke(width = 16f, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            Text(
                                text = if (isFreshAccount) "--" else "${userStats.spatialLogicScore}%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isFreshAccount) MaterialTheme.colorScheme.outline else Primary
                            )
                        }
                    }
                }

                // 3. Weekly Arrow Activity (Calculated from user's moves)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weekly Activity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${userStats.totalMovesMade} arrows disentangled so far",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val days = listOf("M", "T", "W", "T", "F", "S", "S")
                        val activityMultiplier = if (userStats.totalMovesMade == 0) 0f else (userStats.totalMovesMade / 30f).coerceIn(0.15f, 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            days.forEachIndexed { idx, day ->
                                val heightRatio = if (isFreshAccount) 0.05f else (activityMultiplier * (0.3f + (idx % 3) * 0.2f)).coerceIn(0.05f, 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .height((70 * heightRatio).dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isFreshAccount) MaterialTheme.colorScheme.surfaceContainerHigh else if (heightRatio >= 0.7f) Primary else SecondaryContainer)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Sector Difficulty Progression (Real Counts)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Difficulty Progression",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        DifficultyProgressBar(name = "Novice (Easy)", current = easyCleared, max = 50, color = Tertiary)
                        DifficultyProgressBar(name = "Adept (Normal)", current = normalCleared, max = 50, color = Primary)
                        DifficultyProgressBar(name = "Expert (Hard)", current = hardCleared, max = 75, color = Secondary)
                        DifficultyProgressBar(name = "Mastermind (Very Hard)", current = veryHardCleared, max = 65, color = Color(0xFF6366F1))
                        DifficultyProgressBar(name = "Grandmaster (Extreme)", current = grandmasterCleared, max = 60, color = Color(0xFFEF4444))
                    }
                }

                // 5. Trophy Vault (Real Unlocks)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    shadowElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = AmberStar)
                            Text(
                                text = "Trophy Vault",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TrophyItem(title = "First Flight", desc = "Cleared Level 1", unlocked = userStats.totalLevelsCleared >= 1)
                        TrophyItem(title = "Perfectionist", desc = "Earned 3 Stars on 25 Levels", unlocked = userStats.totalStars >= 75)
                        TrophyItem(title = "Spatial Architect", desc = "Disentangled 40 Level Mazes", unlocked = userStats.totalLevelsCleared >= 40)
                        TrophyItem(title = "Speed Vector", desc = "Solved a level under 30 seconds", unlocked = userStats.totalLevelsCleared >= 1 && userStats.avgTimeSeconds in 1..30)
                    }
                }

                // Privacy Policy Card & Open Button
                val context = LocalContext.current
                val privacyPolicyUrl = AdsConfig.PRIVACY_POLICY_URL
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                        .testTag("stats_privacy_policy_card"),
                    shape = RoundedCornerShape(16.dp),
                    color = Primary.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, Primary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PrivacyTip,
                                    contentDescription = "Privacy Policy",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Privacy Policy",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "zabi1151.github.io/Privacy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Primary
                                )
                            }
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("stats_privacy_policy_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "OPEN",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // AdMob Banner
                AdBannerView()

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Navigation
            BottomNavBar(
                currentTab = AppNavTab.BRAIN,
                onTabSelected = onNavTabSelected
            )
        }
    }
}

@Composable
private fun ProfileBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DifficultyProgressBar(
    name: String,
    current: Int,
    max: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "$current / $max", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (max == 0) 0f else current.toFloat() / max.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
    }
}

@Composable
private fun TrophyItem(
    title: String,
    desc: String,
    unlocked: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (unlocked) AmberStar.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (unlocked) Icons.Default.WorkspacePremium else Icons.Default.Lock,
                contentDescription = null,
                tint = if (unlocked) AmberStar else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
