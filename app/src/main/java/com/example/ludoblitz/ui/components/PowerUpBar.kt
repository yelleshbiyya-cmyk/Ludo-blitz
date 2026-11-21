package com.example.ludoblitz.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.ludoblitz.model.PowerUpType
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight

@Composable
fun PowerUpBar(
    chargesMap: Map<PowerUpType, Int>,
    onPowerUpUsed: (PowerUpType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PowerUpType.entries.forEach { powerUp ->
            val charges = chargesMap[powerUp] ?: 0
            val isEnabled = charges > 0

            PowerUpButton(
                powerUp = powerUp,
                charges = charges,
                isEnabled = isEnabled,
                onClick = { onPowerUpUsed(powerUp) }
            )
        }
    }
}

@Composable
private fun PowerUpButton(
    powerUp: PowerUpType,
    charges: Int,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val (emoji, topColor, bottomColor, ledgeColor) = when (powerUp) {
        PowerUpType.SPEED_BOOST -> Quad("⚡", Color(0xFF818CF8), Color(0xFF4F46E5), Color(0xFF312E81))
        PowerUpType.SHIELD -> Quad("🛡️", Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF0C4A6E))
        PowerUpType.DOUBLE_DICE -> Quad("🎲", Color(0xFFFDE047), Color(0xFFD97706), Color(0xFF78350F))
        PowerUpType.EXTRA_TURN -> Quad("🔄", Color(0xFFFB923C), Color(0xFFEA580C), Color(0xFF7C2D12))
        PowerUpType.SAFE_STEP -> Quad("⭐", Color(0xFF34D399), Color(0xFF059669), Color(0xFF064E3B))
        PowerUpType.BLITZ_MOVE -> Quad("🚀", Color(0xFFF472B6), Color(0xFFDB2777), Color(0xFF831843))
    }

    Box(modifier = Modifier.size(50.dp), contentAlignment = Alignment.Center) {
        Game3DButton(
            onClick = onClick,
            enabled = isEnabled,
            topColor = topColor,
            bottomColor = bottomColor,
            ledgeColor = ledgeColor,
            borderColor = if (isEnabled) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.25f),
            cornerRadius = 15.dp,
            height = 48.dp,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .size(48.dp)
                .testTag("powerup_${powerUp.name.lowercase()}")
        ) {
            Text(emoji, fontSize = 20.sp)
        }

        // Gleaming Golden Charge Badge
        if (charges > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(RoyalGoldLight, RoyalGold)))
                    .border(1.2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$charges",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E1B4B)
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
