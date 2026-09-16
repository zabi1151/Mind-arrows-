package com.example.ui.screens.gameplay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdBannerView
import com.example.data.model.UserStatsEntity
import com.example.ui.theme.ErrorColor
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryContainer
import com.example.ui.theme.Secondary
import com.example.ui.theme.Tertiary
import com.example.ui.viewmodel.ActiveGameState

@Composable
fun GameplayScreen(
    gameState: ActiveGameState,
    userStats: UserStatsEntity,
    onArrowClick: (com.example.game.model.Arrow, Boolean) -> Unit,
    onUndoClick: () -> Unit,
    onHintClick: () -> Unit,
    onRestartClick: () -> Unit,
    onBackClick: () -> Unit,
    onPauseClick: () -> Unit,
    onToggleSound: () -> Unit,
    onWatchAdForHint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = gameState.level

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("gameplay_screen_surface"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top HUD Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(2.dp, RoundedCornerShape(12.dp))
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .clickable(onClick = onBackClick)
                        .testTag("gameplay_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Sector & Level Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sector 0${(level.id - 1) / 50 + 1} • Lvl ${level.id}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ACTIVE PUZZLE CANVAS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Primary
                    )
                }

                // Audio & Pause Controls
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val isAudioActive = userStats.soundEnabled || userStats.musicEnabled
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .clickable(onClick = onToggleSound)
                            .testTag("audio_toggle_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAudioActive) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Audio FX",
                            tint = if (isAudioActive) Primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(2.dp, RoundedCornerShape(12.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .clickable(onClick = onPauseClick)
                            .testTag("pause_game_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Tactical Strategy Callout
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Strategy: Tap unblocked arrows first to clear paths for others.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Metrics HUD Bar (Time, Moves, Lives)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    icon = Icons.Default.Timer,
                    label = "Time",
                    value = formatTime(gameState.elapsedSeconds),
                    tint = Secondary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    icon = Icons.Default.SwapHoriz,
                    label = "Moves",
                    value = "${gameState.movesCount} / ${level.targetMoves}",
                    tint = Primary,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    icon = Icons.Default.Favorite,
                    label = "Lives",
                    value = "${gameState.lives} / ${gameState.maxLives}",
                    tint = ErrorColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. Center Puzzle Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                PuzzleBoardView(
                    gridSize = level.gridSize,
                    arrows = gameState.remainingArrows,
                    hintArrowId = gameState.hintArrowId,
                    onArrowClick = onArrowClick,
                    equippedSkinId = userStats.equippedArrowSkin
                )
            }

            // 5. Objective Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    text = "Disentangle All ${gameState.remainingArrows.size} Remaining Arrows",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // 6. Bottom Action Dock (Undo, Hint, Restart)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Undo Button
                ActionDockButton(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    label = "Undo",
                    badgeText = "↺",
                    onClick = onUndoClick,
                    tag = "action_undo"
                )

                // Hero Hint Button (Glowing, larger)
                HeroHintButton(
                    hintsRemaining = userStats.hintsRemaining,
                    onHintClick = onHintClick,
                    onWatchAdForHint = onWatchAdForHint
                )

                // Restart Button
                ActionDockButton(
                    icon = Icons.Default.Refresh,
                    label = "Reset",
                    badgeText = null,
                    onClick = onRestartClick,
                    tag = "action_restart"
                )
            }

            // 7. Non-Intrusive AdMob Banner Unit
            AdBannerView(modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun MetricCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ActionDockButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeText: String?,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .clickable(onClick = onClick)
                .testTag(tag),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeroHintButton(
    hintsRemaining: Int,
    onHintClick: () -> Unit,
    onWatchAdForHint: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BadgedBox(
            badge = {
                Surface(
                    shape = CircleShape,
                    color = if (hintsRemaining > 0) Tertiary else Primary,
                    shadowElevation = 2.dp
                ) {
                    Text(
                        text = if (hintsRemaining > 0) "$hintsRemaining" else "+2",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Primary)
                    .clickable {
                        if (hintsRemaining > 0) onHintClick() else onWatchAdForHint()
                    }
                    .testTag("action_hint"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Hint",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hintsRemaining > 0) "Hint" else "Free Ad Hint",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Primary
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
