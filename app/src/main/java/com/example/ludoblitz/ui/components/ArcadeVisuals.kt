package com.example.ludoblitz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class CurrencyBurstEvent(
    val id: Long = System.currentTimeMillis(),
    val coins: Int = 0,
    val gems: Int = 0,
    val label: String = ""
)

/**
 * High-definition, Canvas-rendered golden Duck Coin with 3D bevel, embossed cute duck emblem,
 * orange bill, eye glint, and optional animated shimmer.
 */
@Composable
fun HdDuckCoinIcon(
    size: Dp = 24.dp,
    animated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val shimmerPhase = if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "duckCoinShimmer")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer"
        )
        phase
    } else {
        0.35f
    }

    Canvas(modifier = modifier.size(size)) {
        drawHdDuckCoin(
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            radius = this.size.minDimension / 2f,
            shimmerPhase = shimmerPhase
        )
    }
}

fun DrawScope.drawHdDuckCoin(
    center: Offset,
    radius: Float,
    shimmerPhase: Float = 0.35f
) {
    // Outer soft glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFDE047).copy(alpha = 0.45f), Color.Transparent),
            center = center,
            radius = radius * 1.25f
        ),
        radius = radius * 1.25f,
        center = center
    )

    // 3D Coin rim / edge
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFF92400E)),
            start = Offset(center.x - radius, center.y - radius),
            end = Offset(center.x + radius, center.y + radius)
        ),
        radius = radius,
        center = center
    )

    // Inner recessed gold face
    val innerR = radius * 0.84f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFCD34D), Color(0xFFD97706)),
            center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
            radius = innerR * 1.4f
        ),
        radius = innerR,
        center = center
    )

    // Milled inner ring
    drawCircle(
        color = Color(0xFFB45309).copy(alpha = 0.6f),
        radius = innerR * 0.92f,
        center = center,
        style = Stroke(width = (radius * 0.08f).coerceAtLeast(1f))
    )

    // Cute embossed Duck silhouette in the center!
    // Duck body (oval)
    val bodyW = radius * 0.88f
    val bodyH = radius * 0.56f
    drawOval(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFFFF), Color(0xFFFEF3C7))
        ),
        topLeft = Offset(center.x - bodyW * 0.56f, center.y + radius * 0.02f),
        size = Size(bodyW, bodyH)
    )
    // Wing highlight
    drawOval(
        color = Color(0xFFF59E0B).copy(alpha = 0.35f),
        topLeft = Offset(center.x - bodyW * 0.44f, center.y + radius * 0.14f),
        size = Size(bodyW * 0.56f, bodyH * 0.56f)
    )

    // Duck head
    val headCenter = Offset(center.x + radius * 0.14f, center.y - radius * 0.18f)
    val headR = radius * 0.30f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, Color(0xFFFEF08A)),
            center = Offset(headCenter.x - headR * 0.25f, headCenter.y - headR * 0.25f),
            radius = headR * 1.3f
        ),
        radius = headR,
        center = headCenter
    )

    // Duck orange bill
    val billPath = Path().apply {
        moveTo(headCenter.x + headR * 0.72f, headCenter.y - headR * 0.15f)
        lineTo(headCenter.x + headR * 1.48f, headCenter.y + headR * 0.05f)
        lineTo(headCenter.x + headR * 0.72f, headCenter.y + headR * 0.36f)
        close()
    }
    drawPath(billPath, color = Color(0xFFEA580C), style = Fill)

    // Duck eye
    drawCircle(
        color = Color(0xFF1E293B),
        radius = (radius * 0.075f).coerceAtLeast(1.2f),
        center = Offset(headCenter.x + headR * 0.24f, headCenter.y - headR * 0.14f)
    )
    drawCircle(
        color = Color.White,
        radius = (radius * 0.03f).coerceAtLeast(0.6f),
        center = Offset(headCenter.x + headR * 0.28f, headCenter.y - headR * 0.18f)
    )

    // Animated diagonal gloss sparkle
    val glintX = center.x + (shimmerPhase * 2f - 1f) * radius * 0.65f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White.copy(alpha = 0.75f), Color.Transparent),
            center = Offset(glintX, center.y - radius * 0.45f),
            radius = radius * 0.45f
        ),
        radius = radius * 0.45f,
        center = Offset(glintX, center.y - radius * 0.45f)
    )
}

/**
 * High-definition multi-faceted cyan/sapphire Diamond Gem drawn with Canvas.
 */
@Composable
fun HdGemIcon(
    size: Dp = 24.dp,
    animated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val pulse = if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "gemSparkle")
        val p by infiniteTransition.animateFloat(
            initialValue = 0.75f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "gemPulse"
        )
        p
    } else {
        0.9f
    }

    Canvas(modifier = modifier.size(size)) {
        drawHdGem(
            center = Offset(this.size.width / 2f, this.size.height / 2f),
            radius = this.size.minDimension / 2f,
            sparkleAlpha = pulse
        )
    }
}

fun DrawScope.drawHdGem(
    center: Offset,
    radius: Float,
    sparkleAlpha: Float = 0.9f
) {
    // Aura glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.45f * sparkleAlpha), Color.Transparent),
            center = center,
            radius = radius * 1.25f
        ),
        radius = radius * 1.25f,
        center = center
    )

    val topY = center.y - radius * 0.82f
    val midY = center.y - radius * 0.18f
    val botY = center.y + radius * 0.92f
    val topHalf = radius * 0.56f
    val midHalf = radius * 0.92f

    // Base diamond silhouette
    val outlinePath = Path().apply {
        moveTo(center.x - topHalf, topY)
        lineTo(center.x + topHalf, topY)
        lineTo(center.x + midHalf, midY)
        lineTo(center.x, botY)
        lineTo(center.x - midHalf, midY)
        close()
    }
    drawPath(
        path = outlinePath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8), Color(0xFF0284C7), Color(0xFF4338CA)),
            start = Offset(center.x - midHalf, topY),
            end = Offset(center.x + midHalf, botY)
        )
    )

    // Left crown facet
    val leftCrown = Path().apply {
        moveTo(center.x - topHalf, topY)
        lineTo(center.x - topHalf * 0.45f, midY)
        lineTo(center.x - midHalf, midY)
        close()
    }
    drawPath(leftCrown, color = Color(0xFFE0F2FE).copy(alpha = 0.85f))

    // Center table facet
    val tablePath = Path().apply {
        moveTo(center.x - topHalf, topY)
        lineTo(center.x + topHalf, topY)
        lineTo(center.x + topHalf * 0.45f, midY)
        lineTo(center.x - topHalf * 0.45f, midY)
        close()
    }
    drawPath(tablePath, color = Color(0xFFF0F9FF).copy(alpha = 0.72f))

    // Lower center facet
    val lowerCenter = Path().apply {
        moveTo(center.x - topHalf * 0.45f, midY)
        lineTo(center.x + topHalf * 0.45f, midY)
        lineTo(center.x, botY)
        close()
    }
    drawPath(
        lowerCenter,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF38BDF8), Color(0xFF1D4ED8))
        )
    )

    // Crisp facet lines
    drawPath(outlinePath, color = Color.White.copy(alpha = 0.9f), style = Stroke(width = (radius * 0.07f).coerceAtLeast(1f)))

    // Specular star glint
    val glintCenter = Offset(center.x - radius * 0.24f, topY + radius * 0.28f)
    drawCircle(
        color = Color.White.copy(alpha = sparkleAlpha),
        radius = (radius * 0.14f).coerceAtLeast(1.5f),
        center = glintCenter
    )
}

/**
 * Full-screen bright, vibrant, luminous Royal 3D Arcade backdrop with radiant electric-blue
 * to royal-purple gradients, glowing golden sun-rays, and gently drifting stars & sparkles.
 */
@Composable
fun AnimatedArcadeBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arcadeBg")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1D4ED8), // Bright Electric Royal Blue top
                        Color(0xFF312E81), // Luminous Royal Indigo
                        Color(0xFF4C1D95), // Rich Vibrant Jewel Purple
                        Color(0xFF1E296B)  // Clean Sapphire base
                    )
                )
            )
    ) {
        val w = size.width
        val h = size.height
        val angle = drift * 2f * PI.toFloat()

        // Bright Sky-Cyan sunlit bloom at top center
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.42f), Color.Transparent),
                center = Offset(w * 0.50f + cos(angle) * w * 0.07f, h * 0.12f),
                radius = w * 0.78f * pulse
            ),
            radius = w * 0.78f * pulse,
            center = Offset(w * 0.50f + cos(angle) * w * 0.07f, h * 0.12f)
        )

        // Radiant Magenta / Ruby Jewel bloom right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEC4899).copy(alpha = 0.28f), Color.Transparent),
                center = Offset(w * 0.88f - sin(angle) * w * 0.07f, h * 0.40f + cos(angle) * h * 0.05f),
                radius = w * 0.62f
            ),
            radius = w * 0.62f,
            center = Offset(w * 0.88f - sin(angle) * w * 0.07f, h * 0.40f + cos(angle) * h * 0.05f)
        )

        // Royal Gold warm glow bottom-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFACC15).copy(alpha = 0.24f), Color.Transparent),
                center = Offset(w * 0.20f + sin(angle) * w * 0.09f, h * 0.78f),
                radius = w * 0.65f * pulse
            ),
            radius = w * 0.65f * pulse,
            center = Offset(w * 0.20f + sin(angle) * w * 0.09f, h * 0.78f)
        )

        // Emerald / Cyan jewel accent bottom-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF22D3EE).copy(alpha = 0.24f), Color.Transparent),
                center = Offset(w * 0.82f, h * 0.85f - cos(angle) * h * 0.04f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.82f, h * 0.85f - cos(angle) * h * 0.04f)
        )

        // Subtle celebratory sunbeam rays from top center
        val rayOrigin = Offset(w * 0.5f, -h * 0.08f)
        for (r in 0 until 6) {
            val rayAngle = (0.35f + r * 0.24f + sin(angle * 0.5f) * 0.04f)
            val rayEnd = Offset(
                rayOrigin.x + cos(rayAngle) * h * 1.15f,
                rayOrigin.y + sin(rayAngle) * h * 1.15f
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.White.copy(alpha = 0.09f), Color.Transparent),
                    start = rayOrigin,
                    end = rayEnd
                ),
                start = rayOrigin,
                end = rayEnd,
                strokeWidth = w * 0.09f
            )
        }

        // Crisp floating golden sparkles, diamonds & stars
        for (i in 0 until 20) {
            val xFrac = ((i * 73 + 19) % 94 + 3) / 100f
            val yProgress = ((drift + i * 0.052f) % 1f)
            val px = w * xFrac + sin(yProgress * 2f * PI.toFloat() + i) * 16f
            val py = h * (1f - yProgress)
            val alpha = sin(yProgress * PI.toFloat()).coerceIn(0f, 1f) * 0.45f

            if (i % 5 == 0) {
                drawCircle(
                    color = Color(0xFFFDE047).copy(alpha = alpha * 0.75f),
                    radius = 6.5f,
                    center = Offset(px, py)
                )
            } else if (i % 5 == 2) {
                drawCircle(
                    color = Color(0xFF67E8F9).copy(alpha = alpha * 0.85f),
                    radius = 4.5f,
                    center = Offset(px, py)
                )
            } else {
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = if (i % 2 == 0) 2.8f else 1.8f,
                    center = Offset(px, py)
                )
            }
        }
    }
}

private data class FlyingParticleSpec(
    val isGem: Boolean,
    val startOffsetX: Float,
    val startOffsetY: Float,
    val controlOffsetX: Float,
    val controlOffsetY: Float,
    val delayFraction: Float,
    val spinTurns: Float,
    val baseSizeDp: Float
)

/**
 * Full-screen overlay that animates flying HD Duck Coins and Gems along curved Bezier trajectories
 * into the TopHeader's Duck Coin & Gem counters whenever rewards are earned or returning Home!
 */
@Composable
fun CurrencyFlyOverlay(
    burstEvent: CurrencyBurstEvent?,
    onBurstCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (burstEvent == null) return

    val progress = remember(burstEvent.id) { Animatable(0f) }
    val coinParticleCount = when {
        burstEvent.coins <= 0 -> 0
        burstEvent.coins >= 200 -> 14
        else -> 10
    }
    val gemParticleCount = when {
        burstEvent.gems <= 0 -> 0
        burstEvent.gems >= 10 -> 7
        else -> 4
    }

    val particles = remember(burstEvent.id) {
        val list = mutableListOf<FlyingParticleSpec>()
        for (i in 0 until coinParticleCount) {
            val angle = (i.toFloat() / coinParticleCount.coerceAtLeast(1)) * 2f * PI.toFloat()
            list.add(
                FlyingParticleSpec(
                    isGem = false,
                    startOffsetX = cos(angle) * 90f,
                    startOffsetY = sin(angle) * 70f,
                    controlOffsetX = ((i % 5) - 2) * 110f,
                    controlOffsetY = -120f - (i % 3) * 60f,
                    delayFraction = (i * 0.032f).coerceAtMost(0.35f),
                    spinTurns = 2f + (i % 3),
                    baseSizeDp = 28f + (i % 3) * 4f
                )
            )
        }
        for (j in 0 until gemParticleCount) {
            val angle = (j.toFloat() / gemParticleCount.coerceAtLeast(1)) * 2f * PI.toFloat() + 0.4f
            list.add(
                FlyingParticleSpec(
                    isGem = true,
                    startOffsetX = cos(angle) * 75f,
                    startOffsetY = sin(angle) * 65f,
                    controlOffsetX = ((j % 3) - 1) * 130f,
                    controlOffsetY = -150f - (j % 2) * 70f,
                    delayFraction = 0.08f + (j * 0.045f).coerceAtMost(0.38f),
                    spinTurns = 1.5f + (j % 2),
                    baseSizeDp = 26f + (j % 2) * 5f
                )
            )
        }
        list.toList()
    }

    LaunchedEffect(burstEvent.id) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 820, easing = FastOutSlowInEasing)
        )
        onBurstCompleted()
    }

    val p = progress.value
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Origin near lower-center of screen
        val originX = widthPx * 0.50f
        val originY = heightPx * 0.62f

        // Targets aligned with TopHeader Duck Coin chip & Gem chip
        val coinTargetX = widthPx * 0.58f
        val coinTargetY = with(density) { 46.dp.toPx() }

        val gemTargetX = widthPx * 0.77f
        val gemTargetY = with(density) { 46.dp.toPx() }

        // Floating celebratory banner pill
        val bannerAlpha = when {
            p < 0.15f -> p / 0.15f
            p > 0.78f -> ((1f - p) / 0.22f).coerceIn(0f, 1f)
            else -> 1f
        }
        val bannerScale = 0.85f + 0.15f * sin((p.coerceIn(0f, 0.5f) / 0.5f) * (PI.toFloat() / 2f))

        if (bannerAlpha > 0.02f) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-30).dp * p)
                    .scale(bannerScale)
                    .alpha(bannerAlpha)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF1E1B4B).copy(alpha = 0.94f), Color(0xFF312E81).copy(alpha = 0.94f))
                        )
                    )
                    .border(2.dp, BentoAmber, RoundedCornerShape(24.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (burstEvent.coins > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HdDuckCoinIcon(size = 24.dp, animated = false)
                            Text(
                                text = "+${burstEvent.coins}",
                                color = BentoAmber,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                    if (burstEvent.gems > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HdGemIcon(size = 24.dp, animated = false)
                            Text(
                                text = "+${burstEvent.gems}",
                                color = BentoSky,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Single-pass Canvas for all flying Duck Coins & Gems (zero per-particle Box/Canvas overhead)
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (spec in particles) {
                val localT = ((p - spec.delayFraction) / (1f - spec.delayFraction)).coerceIn(0f, 1f)
                if (localT <= 0f || localT >= 1f) continue

                val sx = originX + spec.startOffsetX
                val sy = originY + spec.startOffsetY
                val tx = if (spec.isGem) gemTargetX else coinTargetX
                val ty = if (spec.isGem) gemTargetY else coinTargetY

                val cx = (sx + tx) * 0.5f + spec.controlOffsetX
                val cy = (sy + ty) * 0.5f + spec.controlOffsetY

                // Quadratic Bezier curve
                val oneMinusT = 1f - localT
                val x = oneMinusT * oneMinusT * sx + 2f * oneMinusT * localT * cx + localT * localT * tx
                val y = oneMinusT * oneMinusT * sy + 2f * oneMinusT * localT * cy + localT * localT * ty

                // 3D tumble scale + pop arc
                val spinFlip = abs(cos(localT * spec.spinTurns * PI.toFloat())).coerceAtLeast(0.28f)
                val arcScale = (0.72f + 0.52f * sin(localT * PI.toFloat())).coerceAtLeast(0.55f)
                val fadeScale = if (localT > 0.84f) ((1f - localT) / 0.16f).coerceIn(0f, 1f) else 1f
                val radiusPx = (spec.baseSizeDp.dp.toPx() * 0.5f) * fadeScale

                if (radiusPx < 1f) continue

                withTransform({
                    translate(left = x, top = y)
                    rotate(degrees = localT * 180f, pivot = Offset.Zero)
                    scale(scaleX = spinFlip * arcScale, scaleY = arcScale, pivot = Offset.Zero)
                }) {
                    if (spec.isGem) {
                        drawHdGem(center = Offset.Zero, radius = radiusPx, sparkleAlpha = 0.95f)
                    } else {
                        drawHdDuckCoin(center = Offset.Zero, radius = radiusPx, shimmerPhase = localT)
                    }
                }
            }
        }
    }
}
