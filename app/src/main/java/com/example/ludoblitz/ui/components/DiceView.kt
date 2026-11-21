package com.example.ludoblitz.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludoblitz.model.PlayerSide
import kotlinx.coroutines.delay

/**
 * Dynamic player-colored 3D Dice roller:
 * - Changes body, border, glow, and pip styling to match the active player (Red/Green/Yellow/Blue)
 *   so players immediately identify whose turn it is.
 * - Cycles crisp tumbling pip faces while rolling and pops with a bouncy settle on landing.
 */
@Composable
fun DiceView(
    diceValue: Int,
    secondDiceValue: Int,
    isDoubleDice: Boolean,
    isRolling: Boolean,
    canRoll: Boolean,
    currentTurn: PlayerSide,
    onRollClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation = remember { Animatable(0f) }
    val shakeOffset = remember { Animatable(0f) }
    val landPop = remember { Animatable(1f) }
    var rollingDisplayFace by remember { mutableIntStateOf(diceValue.coerceIn(1, 6)) }

    val infiniteTransition = rememberInfiniteTransition(label = "dicePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diceScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    // Smoothly animate dice palette to the active player's color!
    val targetLight = when (currentTurn) {
        PlayerSide.RED -> Color(0xFFFF8FA3)
        PlayerSide.GREEN -> Color(0xFF6EE7B7)
        PlayerSide.YELLOW -> Color(0xFFFEF08A)
        PlayerSide.BLUE -> Color(0xFF93C5FD)
    }
    val targetMain = when (currentTurn) {
        PlayerSide.RED -> Color(0xFFF43F5E)
        PlayerSide.GREEN -> Color(0xFF10B981)
        PlayerSide.YELLOW -> Color(0xFFFBBF24)
        PlayerSide.BLUE -> Color(0xFF3B82F6)
    }
    val targetDark = when (currentTurn) {
        PlayerSide.RED -> Color(0xFF9F1239)
        PlayerSide.GREEN -> Color(0xFF047857)
        PlayerSide.YELLOW -> Color(0xFFB45309)
        PlayerSide.BLUE -> Color(0xFF1D4ED8)
    }

    val diceLight by animateColorAsState(targetLight, tween(280), label = "diceLight")
    val diceMain by animateColorAsState(targetMain, tween(280), label = "diceMain")
    val diceDark by animateColorAsState(targetDark, tween(280), label = "diceDark")

    val isBrightYellow = currentTurn == PlayerSide.YELLOW

    LaunchedEffect(isRolling) {
        if (isRolling) {
            rotation.snapTo(0f)
            // Spin continuously while cycling real dice faces so roll feels crisp and lively
            val startFaces = listOf(2, 5, 3, 6, 1, 4, 2, 6)
            var idx = 0
            while (true) {
                rollingDisplayFace = startFaces[idx % startFaces.size]
                idx++
                rotation.animateTo(
                    targetValue = rotation.value + 90f,
                    animationSpec = tween(durationMillis = 72, easing = LinearEasing)
                )
            }
        } else {
            rotation.snapTo(0f)
            landPop.snapTo(1.26f)
            landPop.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    LaunchedEffect(canRoll, currentTurn) {
        if (canRoll) {
            while (true) {
                shakeOffset.animateTo(5f, tween(70))
                shakeOffset.animateTo(-5f, tween(70))
                shakeOffset.animateTo(3f, tween(70))
                shakeOffset.animateTo(0f, tween(70))
                delay(1200)
            }
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    val displayedValue = if (isRolling) rollingDisplayFace else diceValue

    Row(
        modifier = modifier.testTag("dice_roller_container"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SingleDiceCube(
                value = displayedValue,
                isRolling = isRolling,
                rotationDeg = if (isRolling) rotation.value else shakeOffset.value * 2f,
                canRoll = canRoll,
                diceLight = diceLight,
                diceMain = diceMain,
                diceDark = diceDark,
                isBrightYellow = isBrightYellow,
                pulseScale = (if (canRoll) pulseScale else 1f) * landPop.value,
                glowAlpha = glowAlpha,
                onClick = onRollClicked
            )

            // Crisp active badge below dice showing ROLL / ROLLING / Rolled Value
            val badgeText = when {
                isRolling -> "ROLLING..."
                canRoll -> "TAP ROLL"
                diceValue == 6 -> "★ SIX! ★"
                else -> "ROLLED $diceValue"
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(diceDark.copy(alpha = if (canRoll) 0.95f else 0.72f))
                    .border(
                        width = 1.dp,
                        color = if (canRoll) Color.White.copy(alpha = glowAlpha) else diceLight.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )
            }
        }

        if (isDoubleDice && secondDiceValue > 0) {
            SingleDiceCube(
                value = if (isRolling) ((rollingDisplayFace % 6) + 1) else secondDiceValue,
                isRolling = isRolling,
                rotationDeg = if (isRolling) -rotation.value else 0f,
                canRoll = false,
                diceLight = diceLight,
                diceMain = diceMain,
                diceDark = diceDark,
                isBrightYellow = isBrightYellow,
                pulseScale = landPop.value,
                glowAlpha = 1f,
                onClick = {}
            )
        }
    }
}

@Composable
private fun SingleDiceCube(
    value: Int,
    isRolling: Boolean,
    rotationDeg: Float,
    canRoll: Boolean,
    diceLight: Color,
    diceMain: Color,
    diceDark: Color,
    isBrightYellow: Boolean,
    pulseScale: Float,
    glowAlpha: Float,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .scale(pulseScale)
            .rotate(rotationDeg)
            .shadow(
                elevation = if (canRoll || isRolling) 20.dp else 10.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = diceMain,
                spotColor = diceLight
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(diceLight, diceMain, diceDark),
                    radius = 150f
                )
            )
            .border(
                width = if (canRoll || isRolling) 3.2.dp else 2.dp,
                color = if (canRoll) Color.White.copy(alpha = glowAlpha) else diceLight.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                enabled = canRoll,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                onClick()
            }
            .testTag("single_dice_cube"),
        contentAlignment = Alignment.Center
    ) {
        // Inner glossy recessed face for authentic 3D beveled dice feel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isBrightYellow) 0.42f else 0.22f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.20f)
                        )
                    )
                )
        ) {
            DicePipsLayout(
                value = value,
                isBrightYellow = isBrightYellow
            )
        }
    }
}

@Composable
private fun DicePipsLayout(
    value: Int,
    isBrightYellow: Boolean
) {
    // High-contrast glossy pips: crisp white on Red/Green/Blue, deep royal navy on bright Yellow
    val pipColor = if (isBrightYellow) Color(0xFF1E1B4B) else Color.White
    val pipShadow = if (isBrightYellow) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.35f)
    val pipSize = 10.5.dp

    @Composable
    fun Pip(modifier: Modifier, isCenterLarge: Boolean = false) {
        Box(
            modifier = modifier
                .size(if (isCenterLarge) 17.dp else pipSize)
                .shadow(2.dp, CircleShape, ambientColor = pipShadow, spotColor = pipShadow)
                .clip(CircleShape)
                .background(pipColor)
                .border(0.8.dp, pipShadow, CircleShape)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(9.dp)
    ) {
        when (value) {
            1 -> {
                Pip(modifier = Modifier.align(Alignment.Center), isCenterLarge = true)
            }
            2 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart))
                Pip(modifier = Modifier.align(Alignment.BottomEnd))
            }
            3 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart))
                Pip(modifier = Modifier.align(Alignment.Center))
                Pip(modifier = Modifier.align(Alignment.BottomEnd))
            }
            4 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart))
                Pip(modifier = Modifier.align(Alignment.TopEnd))
                Pip(modifier = Modifier.align(Alignment.BottomStart))
                Pip(modifier = Modifier.align(Alignment.BottomEnd))
            }
            5 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart))
                Pip(modifier = Modifier.align(Alignment.TopEnd))
                Pip(modifier = Modifier.align(Alignment.Center))
                Pip(modifier = Modifier.align(Alignment.BottomStart))
                Pip(modifier = Modifier.align(Alignment.BottomEnd))
            }
            6 -> {
                Pip(modifier = Modifier.align(Alignment.TopStart))
                Pip(modifier = Modifier.align(Alignment.TopEnd))
                Pip(modifier = Modifier.align(Alignment.CenterStart))
                Pip(modifier = Modifier.align(Alignment.CenterEnd))
                Pip(modifier = Modifier.align(Alignment.BottomStart))
                Pip(modifier = Modifier.align(Alignment.BottomEnd))
            }
            else -> {
                Text(
                    text = "$value",
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp,
                    color = pipColor,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
