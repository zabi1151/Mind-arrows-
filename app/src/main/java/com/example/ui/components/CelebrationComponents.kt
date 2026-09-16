package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberStar
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.Tertiary
import java.util.Random

@Composable
fun StarRatingRow(
    stars: Int,
    maxStars: Int = 3,
    isHeroDisplay: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (isHeroDisplay) {
        val infiniteTransition = rememberInfiniteTransition(label = "stars")
        val heroScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heroStarPulse"
        )

        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star 1 (Tilted left)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .rotate(-12f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (stars >= 1) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (stars >= 1) AmberStar else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Star 2 (Hero Center, larger, pulsing)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = (-4).dp)
                    .scale(heroScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (stars >= 2) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (stars >= 2) AmberStar else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(46.dp)
                )
            }

            // Star 3 (Tilted right)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .rotate(12f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (stars >= 3) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (stars >= 3) AmberStar else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 1..maxStars) {
                Icon(
                    imageVector = if (i <= stars) Icons.Default.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (i <= stars) AmberStar else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val color: Color
)

@Composable
fun CelebrationConfetti(
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val colors = listOf(Primary, Secondary, SecondaryContainer, Tertiary, AmberStar)
        val rng = Random(42)
        (0 until 35).map {
            ConfettiParticle(
                xRatio = rng.nextFloat(),
                yRatio = rng.nextFloat() * 0.75f,
                radius = 4f + rng.nextFloat() * 7f,
                color = colors[rng.nextInt(colors.size)]
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        for (p in particles) {
            drawCircle(
                color = p.color,
                radius = p.radius,
                center = Offset(p.xRatio * w, p.yRatio * h)
            )
        }
    }
}
