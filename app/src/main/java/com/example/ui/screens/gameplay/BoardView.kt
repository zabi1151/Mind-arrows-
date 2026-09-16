package com.example.ui.screens.gameplay

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.PuzzleEngine
import com.example.game.model.Arrow
import com.example.game.model.ArrowSkin
import com.example.game.model.ArrowSkinCatalog
import com.example.game.model.Direction
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.Tertiary
import com.example.ui.theme.TertiaryFixed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun PuzzleBoardView(
    gridSize: Int,
    arrows: List<Arrow>,
    hintArrowId: String?,
    onArrowClick: (Arrow, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    equippedSkinId: String = "classic_cyan"
) {
    val unblockedIds = remember(arrows, gridSize) {
        PuzzleEngine.getUnblockedArrows(arrows, gridSize).map { it.id }.toSet()
    }

    val skin = remember(equippedSkinId) {
        ArrowSkinCatalog.getSkin(equippedSkinId)
    }

    // Board Card Outer Frame
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("puzzle_board_surface"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val boardDimension = min(maxWidth.value, maxHeight.value).dp
            val cellSize = boardDimension / gridSize

            // Exact Board Container with TopStart alignment so coordinates (0,0) start at top-left
            Box(
                modifier = Modifier
                    .size(boardDimension)
            ) {
                // 1. Background Grid Dots
                GridDotsOverlay(gridSize = gridSize, cellSize = cellSize)

                // 2. Perimeter Exit Corridor Beacons
                PerimeterBeacons()

                // 3. Render Each Active Arrow
                arrows.forEach { arrow ->
                    key(arrow.id) {
                        val isUnblocked = arrow.id in unblockedIds
                        val isHinted = arrow.id == hintArrowId

                        ArrowItemView(
                            arrow = arrow,
                            gridSize = gridSize,
                            cellSize = cellSize,
                            skin = skin,
                            isUnblocked = isUnblocked,
                            isHinted = isHinted,
                            onTap = {
                                onArrowClick(arrow, isUnblocked)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridDotsOverlay(
    gridSize: Int,
    cellSize: Dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                Box(
                    modifier = Modifier
                        .offset(x = cellSize * c + (cellSize / 2 - 3.dp), y = cellSize * r + (cellSize / 2 - 3.dp))
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                )
            }
        }
    }
}

@Composable
private fun PerimeterBeacons() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top gate beacon
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.55f)
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Tertiary, Color.Transparent)
                    )
                )
        )
        // Bottom gate beacon
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.55f)
                .height(3.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, Primary, Color.Transparent)
                    )
                )
        )
        // Left gate beacon
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(3.dp)
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Primary, Color.Transparent)
                    )
                )
        )
        // Right gate beacon
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(3.dp)
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Secondary, Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun ArrowItemView(
    arrow: Arrow,
    gridSize: Int,
    cellSize: Dp,
    skin: ArrowSkin,
    isUnblocked: Boolean,
    isHinted: Boolean,
    onTap: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember(arrow.id) { Animatable(0f) }
    val exitOffset = remember(arrow.id) { Animatable(0f) }
    val exitAlpha = remember(arrow.id) { Animatable(1f) }
    var isExiting by remember(arrow.id) { mutableStateOf(false) }

    LaunchedEffect(arrow.id) {
        exitOffset.snapTo(0f)
        exitAlpha.snapTo(1f)
        shakeOffset.snapTo(0f)
        isExiting = false
    }

    // Pulsing animation for Hinted or Unblocked arrow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isHinted || isUnblocked) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowPulse"
    )

    // Calculate bounding box on grid
    val occupied = arrow.occupiedCells()
    val minRow = occupied.minOf { it.row }
    val maxRow = occupied.maxOf { it.row }
    val minCol = occupied.minOf { it.col }
    val maxCol = occupied.maxOf { it.col }

    val isVertical = arrow.direction == Direction.UP || arrow.direction == Direction.DOWN
    // Slim cross-axis inset for sleeker, slimmer arrow profile
    val crossInset = (cellSize * 0.22f).coerceIn(6.dp, 12.dp)
    val alongInset = (cellSize * 0.08f).coerceIn(4.dp, 8.dp)

    val left = if (isVertical) cellSize * minCol + crossInset else cellSize * minCol + alongInset
    val top = if (isVertical) cellSize * minRow + alongInset else cellSize * minRow + crossInset
    val width = if (isVertical) (cellSize * (maxCol - minCol + 1)) - (crossInset * 2) else (cellSize * (maxCol - minCol + 1)) - (alongInset * 2)
    val height = if (isVertical) (cellSize * (maxRow - minRow + 1)) - (alongInset * 2) else (cellSize * (maxRow - minRow + 1)) - (crossInset * 2)

    // Arrow background gradient from equipped skin
    val arrowBrush = when {
        isHinted -> Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFF59E0B)))
        isUnblocked -> Brush.linearGradient(skin.unblockedColors)
        else -> Brush.linearGradient(skin.gradientColors)
    }

    // Direction-based shoot-off vector
    val exitDistance = (cellSize * (gridSize + 3)).value

    Box(
        modifier = Modifier
            .offset(x = left, y = top)
            .size(width = width, height = height)
            .offset {
                val shake = shakeOffset.value.roundToInt()
                val exitX = when (arrow.direction) {
                    Direction.LEFT -> -exitOffset.value
                    Direction.RIGHT -> exitOffset.value
                    else -> 0f
                }.roundToInt()
                val exitY = when (arrow.direction) {
                    Direction.UP -> -exitOffset.value
                    Direction.DOWN -> exitOffset.value
                    else -> 0f
                }.roundToInt()

                IntOffset(
                    x = if (arrow.direction == Direction.UP || arrow.direction == Direction.DOWN) shake else exitX,
                    y = if (arrow.direction == Direction.LEFT || arrow.direction == Direction.RIGHT) shake else exitY
                )
            }
            .scale(if (!isExiting) pulseScale else 1.08f)
            .graphicsLayer {
                alpha = exitAlpha.value
            }
            .shadow(
                elevation = if (isUnblocked || isHinted) 6.dp else 2.dp,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(arrowBrush)
            .border(
                width = if (isHinted) 2.dp else if (isUnblocked) 1.5.dp else 0.5.dp,
                color = if (isHinted) Color(0xFFFFD700) else if (isUnblocked) skin.accentColor else Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = !isExiting) {
                if (isUnblocked) {
                    isExiting = true
                    coroutineScope.launch {
                        // Smooth launch slide out animation
                        val animJob = launch {
                            exitOffset.animateTo(
                                targetValue = exitDistance * 2.5f,
                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                            )
                        }
                        val fadeJob = launch {
                            delay(60)
                            exitAlpha.animateTo(0f, animationSpec = tween(200))
                        }
                        animJob.join()
                        fadeJob.join()
                        onTap()
                    }
                } else {
                    // Shake animation for blocked collision
                    coroutineScope.launch {
                        shakeOffset.animateTo(12f, tween(50))
                        shakeOffset.animateTo(-12f, tween(50))
                        shakeOffset.animateTo(8f, tween(50))
                        shakeOffset.animateTo(-8f, tween(50))
                        shakeOffset.animateTo(0f, tween(50))
                    }
                    onTap()
                }
            }
            .testTag("arrow_${arrow.id}")
    ) {
        // Arrow Body Details: Head circle, decorative connectors, Tail circle
        when (arrow.direction) {
            Direction.UP -> VerticalArrowContent(pointingUp = true, isUnblocked = isUnblocked, skin = skin)
            Direction.DOWN -> VerticalArrowContent(pointingUp = false, isUnblocked = isUnblocked, skin = skin)
            Direction.LEFT -> HorizontalArrowContent(pointingLeft = true, isUnblocked = isUnblocked, skin = skin)
            Direction.RIGHT -> HorizontalArrowContent(pointingLeft = false, isUnblocked = isUnblocked, skin = skin)
        }

        // Tactical "FREE" badge on unblocked arrow
        if (isUnblocked && !isExiting) {
            Surface(
                modifier = Modifier
                    .align(if (arrow.direction == Direction.UP) Alignment.BottomCenter else Alignment.TopCenter)
                    .padding(2.dp),
                shape = RoundedCornerShape(12.dp),
                color = TertiaryFixed,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "FREE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color(0xFF002113),
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun VerticalArrowContent(pointingUp: Boolean, isUnblocked: Boolean, skin: ArrowSkin) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (pointingUp) {
            ArrowHeadCircle(direction = Direction.UP, isUnblocked = isUnblocked, skin = skin)
            ArrowMiddleStems(isVertical = true)
            ArrowTailCircle(isUnblocked = isUnblocked, skin = skin)
        } else {
            ArrowTailCircle(isUnblocked = isUnblocked, skin = skin)
            ArrowMiddleStems(isVertical = true)
            ArrowHeadCircle(direction = Direction.DOWN, isUnblocked = isUnblocked, skin = skin)
        }
    }
}

@Composable
private fun HorizontalArrowContent(pointingLeft: Boolean, isUnblocked: Boolean, skin: ArrowSkin) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (pointingLeft) {
            ArrowHeadCircle(direction = Direction.LEFT, isUnblocked = isUnblocked, skin = skin)
            ArrowMiddleStems(isVertical = false)
            ArrowTailCircle(isUnblocked = isUnblocked, skin = skin)
        } else {
            ArrowTailCircle(isUnblocked = isUnblocked, skin = skin)
            ArrowMiddleStems(isVertical = false)
            ArrowHeadCircle(direction = Direction.RIGHT, isUnblocked = isUnblocked, skin = skin)
        }
    }
}

@Composable
private fun ArrowHeadCircle(direction: Direction, isUnblocked: Boolean, skin: ArrowSkin) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Color.White)
            .shadow(1.5.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val icon = when (direction) {
            Direction.UP -> Icons.Default.KeyboardArrowUp
            Direction.DOWN -> Icons.Default.KeyboardArrowDown
            Direction.LEFT -> Icons.AutoMirrored.Filled.KeyboardArrowLeft
            Direction.RIGHT -> Icons.AutoMirrored.Filled.KeyboardArrowRight
        }
        val tint = if (isUnblocked) Tertiary else skin.gradientColors.first()
        Icon(
            imageVector = icon,
            contentDescription = "Arrow $direction",
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
private fun ArrowTailCircle(isUnblocked: Boolean, skin: ArrowSkin) {
    Box(
        modifier = Modifier
            .size(13.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (isUnblocked) Tertiary else skin.gradientColors.first())
        )
    }
}

@Composable
private fun ArrowMiddleStems(isVertical: Boolean) {
    if (isVertical) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
            )
        }
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
            )
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f))
            )
        }
    }
}
