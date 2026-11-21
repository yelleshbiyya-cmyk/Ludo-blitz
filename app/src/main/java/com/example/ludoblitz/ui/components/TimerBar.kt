package com.example.ludoblitz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoRose
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.RoyalGoldLight

/**
 * Radiant 3D High-Stakes 60-Second Match Countdown Gauge
 */
@Composable
fun TimerBar(
    matchTimeRemaining: Int,
    turnTimeRemaining: Int,
    isFinalTenSeconds: Boolean,
    countdownOverlayNumber: Int?,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "timerPulse")
    val redPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "redPulse"
    )

    val progressFraction = (matchTimeRemaining / 60f).coerceIn(0f, 1f)
    val minutes = matchTimeRemaining / 60
    val seconds = matchTimeRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 1.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .scale(if (isFinalTenSeconds) redPulse else 1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isFinalTenSeconds) listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                        else listOf(Color(0xFF4338CA), Color(0xFF7C3AED))
                    )
                )
                .border(1.dp, if (isFinalTenSeconds) RoyalGoldLight else Color(0xFF93C5FD), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 1.dp)
        ) {
            Text(
                text = if (isFinalTenSeconds) "🔥 FINAL SECONDS" else "⚡ BLITZ LIVE",
                color = if (isFinalTenSeconds) RoyalGoldLight else Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            )
        }

        Text(
            text = timeFormatted,
            color = if (isFinalTenSeconds) RoyalGoldLight else Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp
        )

        // Glossy 3D Progress Bar Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF172554))
                .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.7f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progressFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            when {
                                isFinalTenSeconds -> listOf(Color(0xFFF43F5E), Color(0xFFFDE047))
                                progressFraction < 0.4f -> listOf(BentoAmber, BentoRose)
                                else -> listOf(ElectricCyan, Color(0xFF34D399), RoyalGoldLight)
                            }
                        )
                    )
            )
        }
    }
}

/**
 * 5..4..3..2..1 Radiant 3D Fullscreen Countdown Popout
 */
@Composable
fun CountdownOverlay(
    number: Int?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = number != null && number > 0,
        enter = scaleIn(tween(200)) + fadeIn(),
        exit = scaleOut(tween(200)) + fadeOut(),
        modifier = modifier
    ) {
        if (number != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A).copy(alpha = 0.55f))
                    .testTag("countdown_overlay"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(138.dp)
                        .shadow(26.dp, CircleShape, ambientColor = BentoRose, spotColor = RoyalGoldLight)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFDE047), BentoRose, Color(0xFF9F1239))
                            )
                        )
                        .border(4.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$number",
                        fontSize = 70.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}
