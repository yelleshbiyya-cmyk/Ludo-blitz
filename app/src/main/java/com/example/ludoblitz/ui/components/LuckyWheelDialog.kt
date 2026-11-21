package com.example.ludoblitz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

data class WheelPrize(
    val label: String,
    val emoji: String,
    val coins: Int,
    val gems: Int,
    val color: Color,
    val accentColor: Color,
    val isJackpot: Boolean = false
)

private val LUCKY_WHEEL_PRIZES = listOf(
    WheelPrize("+100", "🦆", coins = 100, gems = 0, color = Color(0xFFF43F5E), accentColor = Color(0xFF9F1239)),
    WheelPrize("+3 💎", "💎", coins = 0, gems = 3, color = Color(0xFF38BDF8), accentColor = Color(0xFF1D4ED8)),
    WheelPrize("+250", "🦆", coins = 250, gems = 0, color = Color(0xFF34D399), accentColor = Color(0xFF047857)),
    WheelPrize("+5 💎", "💎", coins = 0, gems = 5, color = Color(0xFFA855F7), accentColor = Color(0xFF6B21A8)),
    WheelPrize("+150", "🦆", coins = 150, gems = 0, color = Color(0xFFFB923C), accentColor = Color(0xFFC2410C)),
    WheelPrize("+500", "👑", coins = 500, gems = 0, color = Color(0xFF22D3EE), accentColor = Color(0xFF0E7490)),
    WheelPrize("+10 💎", "💎", coins = 0, gems = 10, color = Color(0xFFF472B6), accentColor = Color(0xFFBE185D)),
    WheelPrize("JACKPOT", "🌟", coins = 750, gems = 15, color = Color(0xFFFDE047), accentColor = Color(0xFFD97706), isJackpot = true)
)

@Composable
fun LuckyWheelDialog(
    freeSpinsLeft: Int,
    currentCoins: Int,
    onWheelTick: () -> Unit,
    onPrizeWon: (coins: Int, gems: Int, spinCostCoins: Int, prizeName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var lastWonPrize by remember { mutableStateOf<WheelPrize?>(null) }
    var lastTickSector by remember { mutableIntStateOf(-1) }

    val spinCost = if (freeSpinsLeft > 0) 0 else 100
    val canAffordSpin = freeSpinsLeft > 0 || currentCoins >= spinCost

    val infiniteTransition = rememberInfiniteTransition(label = "wheelLights")
    val bulbPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bulbPhase"
    )
    val sunburstAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sunburst"
    )

    // Fire crisp wooden peg tick sound whenever a wedge crosses the 12 o'clock pointer
    LaunchedEffect(rotation.value) {
        if (isSpinning) {
            val sliceAngle = 360f / LUCKY_WHEEL_PRIZES.size
            val sector = floor(rotation.value / sliceAngle).toInt()
            if (sector != lastTickSector) {
                lastTickSector = sector
                onWheelTick()
            }
        }
    }

    fun triggerSpin() {
        if (isSpinning || !canAffordSpin) return
        isSpinning = true
        lastWonPrize = null
        val costToCharge = spinCost

        // Weighted fun prize selection (generous & exciting, Jackpot ~8%)
        val roll = Random.nextInt(100)
        val targetIndex = when {
            roll < 8 -> 7   // JACKPOT (+750 Coins + 15 Gems)
            roll < 18 -> 5  // +500 Coins
            roll < 28 -> 6  // +10 Gems
            roll < 44 -> 2  // +250 Coins
            roll < 58 -> 3  // +5 Gems
            roll < 72 -> 4  // +150 Coins
            roll < 86 -> 1  // +3 Gems
            else -> 0       // +100 Coins
        }

        val sliceAngle = 360f / LUCKY_WHEEL_PRIZES.size
        val sliceCenter = targetIndex * sliceAngle + sliceAngle * 0.5f
        val jitter = (Random.nextFloat() - 0.5f) * (sliceAngle * 0.45f)
        val currentMod = ((rotation.value % 360f) + 360f) % 360f
        val desiredMod = ((270f - (sliceCenter + jitter)) % 360f + 360f) % 360f
        val deltaToTarget = ((desiredMod - currentMod) + 360f) % 360f
        val extraSpins = 360f * (5 + Random.nextInt(3))
        val targetRotation = rotation.value + extraSpins + deltaToTarget

        scope.launch {
            rotation.animateTo(
                targetValue = targetRotation,
                animationSpec = tween(
                    durationMillis = 3600,
                    easing = CubicBezierEasing(0.10f, 0.86f, 0.16f, 1.0f)
                )
            )
            val won = LUCKY_WHEEL_PRIZES[targetIndex]
            lastWonPrize = won
            isSpinning = false
            onPrizeWon(won.coins, won.gems, costToCharge, "${won.emoji} ${won.label}")
        }
    }

    Dialog(
        onDismissRequest = { if (!isSpinning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Game3DCard(
            modifier = Modifier
                .fillMaxWidth(0.93f)
                .testTag("lucky_wheel_dialog"),
            gradientColors = listOf(Color(0xFF4C1D95), Color(0xFF312E81), Color(0xFF1E3A8A)),
            borderColors = listOf(RoyalGoldLight, BentoAmber, RoyalGoldLight),
            cornerRadius = 28.dp,
            borderWidth = 2.8.dp,
            shadowColor = BentoAmber
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🎡 LUCKY LUDO WHEEL",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = RoyalGoldLight,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (freeSpinsLeft > 0) "🎁 $freeSpinsLeft Free Spins Remaining!" else "Spin for Duck Coins & Gems!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (freeSpinsLeft > 0) Color(0xFF86EFAC) else Color(0xFFE0E7FF)
                        )
                    }
                    IconButton(
                        onClick = { if (!isSpinning) onDismiss() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .border(1.2.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                            .testTag("close_lucky_wheel_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // 3D Royal Gold Spin Wheel + Sunburst Aura + Top Pointer
                Box(
                    modifier = Modifier
                        .size(282.dp)
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val outerR = size.minDimension / 2f - 4f
                        val wheelR = outerR - 18f

                        // Subtle rotating golden sunburst halo behind wheel
                        rotate(degrees = sunburstAngle, pivot = center) {
                            for (i in 0 until 12) {
                                drawArc(
                                    color = RoyalGoldLight.copy(alpha = 0.11f),
                                    startAngle = i * 30f,
                                    sweepAngle = 14f,
                                    useCenter = true,
                                    topLeft = Offset(center.x - outerR - 4f, center.y - outerR - 4f),
                                    size = Size((outerR + 4f) * 2f, (outerR + 4f) * 2f)
                                )
                            }
                        }

                        // Drop Shadow beneath wheel
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.35f),
                            radius = outerR,
                            center = Offset(center.x, center.y + 6f)
                        )

                        // Sculpted Metallic Royal Gold Rim
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(RoyalGoldLight, Color(0xFFF59E0B), Color(0xFF92400E)),
                                center = center,
                                radius = outerR
                            ),
                            radius = outerR,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f),
                            radius = outerR - 1f,
                            center = center,
                            style = Stroke(width = 2.4f)
                        )
                        drawCircle(
                            color = Color(0xFF1E1B4B),
                            radius = wheelR + 4f,
                            center = center
                        )

                        // Glowing Chase LED Bulbs around rim
                        val bulbCount = 16
                        for (i in 0 until bulbCount) {
                            val angleRad = (i * (360.0 / bulbCount)) * PI / 180.0
                            val bx = center.x + (outerR - 8f) * cos(angleRad).toFloat()
                            val by = center.y + (outerR - 8f) * sin(angleRad).toFloat()
                            val isLit = if (i % 2 == 0) bulbPhase > 0.5f else bulbPhase <= 0.5f
                            if (isLit) {
                                drawCircle(
                                    color = RoyalGoldLight.copy(alpha = 0.45f),
                                    radius = 8.5f,
                                    center = Offset(bx, by)
                                )
                            }
                            drawCircle(
                                color = if (isLit) Color.White else Color(0xFF78350F),
                                radius = if (isLit) 4.8f else 3.4f,
                                center = Offset(bx, by)
                            )
                        }

                        // Rotating Glossy 3D Wedges
                        val sliceAngle = 360f / LUCKY_WHEEL_PRIZES.size
                        val topLeft = Offset(center.x - wheelR, center.y - wheelR)
                        val arcSize = Size(wheelR * 2f, wheelR * 2f)

                        rotate(degrees = rotation.value, pivot = center) {
                            LUCKY_WHEEL_PRIZES.forEachIndexed { index, prize ->
                                val startAngle = index * sliceAngle
                                drawArc(
                                    brush = Brush.radialGradient(
                                        colors = listOf(prize.color, prize.accentColor),
                                        center = center,
                                        radius = wheelR
                                    ),
                                    startAngle = startAngle,
                                    sweepAngle = sliceAngle,
                                    useCenter = true,
                                    topLeft = topLeft,
                                    size = arcSize
                                )
                                drawArc(
                                    color = RoyalGoldLight.copy(alpha = 0.9f),
                                    startAngle = startAngle,
                                    sweepAngle = sliceAngle,
                                    useCenter = true,
                                    topLeft = topLeft,
                                    size = arcSize,
                                    style = Stroke(width = 2.6f)
                                )

                                // Draw Crisp Prize Text along wedge midline
                                val midRad = ((startAngle + sliceAngle / 2f) * PI / 180.0)
                                val labelDist = wheelR * 0.65f
                                val tx = center.x + labelDist * cos(midRad).toFloat()
                                val ty = center.y + labelDist * sin(midRad).toFloat()

                                drawContext.canvas.nativeCanvas.apply {
                                    save()
                                    rotate(startAngle + sliceAngle / 2f + 90f, tx, ty)
                                    val textPaint = android.graphics.Paint().apply {
                                        color = if (prize.isJackpot) android.graphics.Color.rgb(30, 27, 75) else android.graphics.Color.WHITE
                                        textSize = if (prize.isJackpot) 23f else 28f
                                        isFakeBoldText = true
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isAntiAlias = true
                                        setShadowLayer(4f, 0f, 2f, android.graphics.Color.argb(140, 0, 0, 0))
                                    }
                                    drawText(prize.label, tx, ty + 8f, textPaint)
                                    restore()
                                }
                            }
                        }

                        // Glossy Glass Specular Overlay on upper wheel face
                        drawArc(
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                            ),
                            startAngle = 195f,
                            sweepAngle = 150f,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize
                        )

                        // Sculpted 3D Metallic Center Hub
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.35f),
                            radius = 33f,
                            center = Offset(center.x, center.y + 4f)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White, RoyalGoldLight, Color(0xFFF59E0B), Color(0xFF92400E)),
                                center = Offset(center.x - 7f, center.y - 7f),
                                radius = 36f
                            ),
                            radius = 32f,
                            center = center
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 32f,
                            center = center,
                            style = Stroke(width = 3.2f)
                        )

                        // Top 12 o'clock Ruby-Gold 3D Pointer Arrow
                        val pointerPath = Path().apply {
                            moveTo(center.x, center.y - wheelR + 20f)
                            lineTo(center.x - 17f, center.y - outerR - 6f)
                            lineTo(center.x + 17f, center.y - outerR - 6f)
                            close()
                        }
                        drawPath(
                            pointerPath,
                            brush = Brush.verticalGradient(listOf(Color(0xFFFB7185), Color(0xFFBE123C)))
                        )
                        drawPath(pointerPath, color = RoyalGoldLight, style = Stroke(width = 3.8f))
                    }

                    // Center Spin Tap Target
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(CircleShape)
                            .clickable(enabled = !isSpinning && canAffordSpin) { triggerSpin() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isSpinning) "⚡" else "SPIN",
                            fontSize = if (isSpinning) 20.sp else 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E1B4B)
                        )
                    }
                }

                // Celebratory Won Prize Banner
                AnimatedVisibility(
                    visible = lastWonPrize != null,
                    enter = fadeIn() + scaleIn()
                ) {
                    val prize = lastWonPrize
                    if (prize != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = RoyalGoldLight)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            prize.color.copy(alpha = 0.55f),
                                            Color(0xFFF59E0B).copy(alpha = 0.45f)
                                        )
                                    )
                                )
                                .border(2.dp, RoyalGoldLight, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when {
                                    prize.isJackpot -> "🌟 JACKPOT! +${prize.coins} Duck Coins & +${prize.gems} Gems!"
                                    prize.coins > 0 -> "🎉 YOU WON +${prize.coins} DUCK COINS!"
                                    else -> "✨ YOU WON +${prize.gems} GEMS!"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Tactile 3D Spin CTA Button
                Game3DButton(
                    onClick = { triggerSpin() },
                    enabled = !isSpinning && canAffordSpin,
                    topColor = if (freeSpinsLeft > 0) Color(0xFF4ADE80) else RoyalGoldLight,
                    bottomColor = if (freeSpinsLeft > 0) Color(0xFF16A34A) else BentoAmber,
                    ledgeColor = if (freeSpinsLeft > 0) Color(0xFF14532D) else RoyalGoldDark,
                    height = 54.dp,
                    cornerRadius = 18.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spin_lucky_wheel_button")
                ) {
                    Text(
                        text = when {
                            isSpinning -> "SPINNING..."
                            freeSpinsLeft > 0 -> "🎡 FREE SPIN ($freeSpinsLeft LEFT)"
                            canAffordSpin -> "🎡 SPIN AGAIN (100 DUCK COINS)"
                            else -> "PLAY A MATCH FOR MORE COINS!"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isSpinning || !canAffordSpin || freeSpinsLeft > 0) Color.White else Color(0xFF1E1B4B),
                        letterSpacing = 0.6.sp
                    )
                }
            }
        }
    }
}
