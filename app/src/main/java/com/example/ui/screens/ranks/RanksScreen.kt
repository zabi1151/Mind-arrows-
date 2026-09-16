package com.example.ui.screens.ranks

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
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

data class LeaderboardPlayer(
    val name: String,
    val levels: Int,
    val stars: Int,
    val isUser: Boolean = false
)

@Composable
fun RanksScreen(
    userStats: UserStatsEntity,
    onNavTabSelected: (AppNavTab) -> Unit,
    onProfileClick: () -> Unit,
    onToggleDarkTheme: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val userDisplayName = if (userStats.userName.isNotBlank()) "${userStats.userName} (You)" else "You"
    // Dynamic ranking based on player's real earned stars & cleared levels
    val allPlayers = remember(userStats.totalStars, userStats.totalLevelsCleared, userStats.userName) {
        listOf(
            LeaderboardPlayer("Sophia Chen", 295, 875),
            LeaderboardPlayer("Marcus Vance", 270, 792),
            LeaderboardPlayer("Elena Rostova", 180, 520),
            LeaderboardPlayer("Liam Gallagher", 120, 340),
            LeaderboardPlayer("Yuki Tanaka", 65, 185),
            LeaderboardPlayer("Lucas Silva", 28, 80),
            LeaderboardPlayer("Aria Thorne", 6, 18),
            LeaderboardPlayer(userDisplayName, userStats.totalLevelsCleared, userStats.totalStars, isUser = true)
        ).sortedWith(compareByDescending<LeaderboardPlayer> { it.stars }.thenByDescending { it.levels })
    }

    val userRank = allPlayers.indexOfFirst { it.isUser } + 1
    val isUnranked = userStats.totalLevelsCleared == 0

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader(
                title = "Global Leaderboard",
                showBackButton = false,
                coins = userStats.coins,
                energy = 45,
                onProfileClick = onProfileClick,
                darkTheme = userStats.darkThemeEnabled,
                onToggleDarkTheme = onToggleDarkTheme,
                onSettingsClick = onSettingsClick
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // Standing card - Starts from Scratch/Null!
                    Surface(
                        modifier = Modifier.fillMaxWidth().testTag("user_standing_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (isUnranked) MaterialTheme.colorScheme.surfaceContainerHigh else AmberStar.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (isUnranked) MaterialTheme.colorScheme.outline else AmberStar,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isUnranked) "Your Standing: Unranked" else "Your Standing: Rank #$userRank",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isUnranked) "Solve your first level to enter the Global Leaderboard! • 0 Stars" else "Global Rank #$userRank • ${userStats.totalStars} Stars earned (${userStats.totalLevelsCleared} Cleared)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                itemsIndexed(allPlayers) { index, player ->
                    val rankNum = index + 1
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (player.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
                        shadowElevation = if (player.isUser) 3.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Rank Badge
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (rankNum) {
                                                1 -> AmberStar.copy(alpha = 0.2f)
                                                2 -> Color(0xFF94A3B8).copy(alpha = 0.2f)
                                                3 -> Color(0xFFD97706).copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (player.isUser && isUnranked) "-" else "#$rankNum",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = when (rankNum) {
                                            1 -> AmberStar
                                            2 -> Color(0xFF64748B)
                                            3 -> Color(0xFFB45309)
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }

                                Column {
                                    Text(
                                        text = player.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (player.isUser) FontWeight.ExtraBold else FontWeight.SemiBold),
                                        color = if (player.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${player.levels} Levels Cleared",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (player.isUser) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AmberStar,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${player.stars}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (player.isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    AdBannerView()
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            BottomNavBar(
                currentTab = AppNavTab.RANKS,
                onTabSelected = onNavTabSelected
            )
        }
    }
}
