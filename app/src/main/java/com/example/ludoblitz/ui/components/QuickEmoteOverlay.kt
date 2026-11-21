package com.example.ludoblitz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class QuickEmoteOption(
    val id: String,
    val emoji: String,
    val label: String
)

data class ActiveEmoteBubble(
    val id: Long,
    val emoji: String,
    val label: String,
    val senderName: String,
    val isFromOpponent: Boolean
)

val BATTLE_QUICK_EMOTES = listOf(
    QuickEmoteOption("fire", "🔥", "On Fire!"),
    QuickEmoteOption("laugh", "😂", "Haha!"),
    QuickEmoteOption("crown", "👑", "Ludo King!"),
    QuickEmoteOption("clap", "👏", "Nice Move!"),
    QuickEmoteOption("cool", "😎", "Too Smooth!"),
    QuickEmoteOption("wow", "😱", "Whoa!")
)

@Composable
fun QuickEmoteTray(
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onSelectEmote: (QuickEmoteOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.85f),
            exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.85f)
        ) {
            Row(
                modifier = Modifier
                    .shadow(12.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xEE181F44))
                    .border(1.5.dp, Color(0xFFFDE047).copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BATTLE_QUICK_EMOTES.forEach { emote ->
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onSelectEmote(emote) }
                            .testTag("emote_btn_${emote.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emote.emoji, fontSize = 18.sp)
                    }
                }
            }
        }

        // Toggle Emote Pill Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        if (expanded) listOf(Color(0xFFF59E0B), Color(0xFFEA580C))
                        else listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                    )
                )
                .border(1.5.dp, Color(0xFFFDE047), CircleShape)
                .clickable { onToggleExpanded() }
                .testTag("toggle_emote_tray_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(if (expanded) "✕" else "😊", fontSize = if (expanded) 14.sp else 18.sp, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun FloatingEmotePopup(
    bubble: ActiveEmoteBubble?,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (bubble == null) return

    val scale = remember(bubble.id) { Animatable(0.3f) }
    val offsetY = remember(bubble.id) { Animatable(18f) }
    val alpha = remember(bubble.id) { Animatable(0f) }

    LaunchedEffect(bubble.id) {
        launch {
            scale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        }
        launch {
            alpha.animateTo(1f, tween(160))
        }
        launch {
            offsetY.animateTo(-10f, tween(500, easing = FastOutSlowInEasing))
        }
        delay(1350L)
        launch {
            offsetY.animateTo(-34f, tween(320))
        }
        alpha.animateTo(0f, tween(300))
        onFinished()
    }

    val borderColor = if (bubble.isFromOpponent) Color(0xFFFBBF24) else Color(0xFF38BDF8)
    val bgBrush = if (bubble.isFromOpponent) {
        Brush.horizontalGradient(listOf(Color(0xF0451A03), Color(0xF01E1B4B)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xF01E3A8A), Color(0xF0312E81)))
    }

    Row(
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .scale(scale.value)
            .alpha(alpha.value)
            .shadow(14.dp, RoundedCornerShape(22.dp), ambientColor = borderColor)
            .clip(RoundedCornerShape(22.dp))
            .background(bgBrush)
            .border(2.dp, borderColor, RoundedCornerShape(22.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(bubble.emoji, fontSize = 24.sp)
        Text(
            text = "${bubble.senderName}: ${bubble.label}",
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black
        )
    }
}
