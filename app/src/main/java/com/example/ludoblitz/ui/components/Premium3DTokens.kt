package com.example.ludoblitz.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.ludoblitz.model.GameBoardConstants
import com.example.ludoblitz.model.GridCoord
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.model.Token
import com.example.ludoblitz.model.TokenState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Computes the exact sequence of grid coordinates a token visits step-by-step
 * between two states so the UI can animate authentic cell-by-cell hopping with
 * synchronized step sounds and particles without altering game rules.
 */
fun computeTokenStepPath(
    prevToken: Token?,
    nextToken: Token,
    isClassic: Boolean
): List<GridCoord> {
    if (prevToken == null) return listOf(GameBoardConstants.getTokenGridCoord(nextToken))
    if (prevToken.state == nextToken.state &&
        prevToken.trackPos == nextToken.trackPos &&
        prevToken.homeStep == nextToken.homeStep
    ) {
        return emptyList()
    }

    // Captured back to base -> direct arc to base
    if (nextToken.state == TokenState.BASE && prevToken.state != TokenState.BASE) {
        return listOf(GameBoardConstants.getTokenGridCoord(nextToken))
    }

    // Leaving base -> jump straight onto start tile
    if (prevToken.state == TokenState.BASE && nextToken.state == TokenState.TRACK) {
        return listOf(GameBoardConstants.getTokenGridCoord(nextToken))
    }

    val steps = mutableListOf<GridCoord>()
    var simState = prevToken.state
    var simTrack = prevToken.trackPos
    var simHome = prevToken.homeStep

    // Walk up to 12 single steps max to reach nextToken position safely
    for (i in 0 until 12) {
        val nextStep = GameBoardConstants.calculateMove(
            side = nextToken.side,
            currentState = simState,
            currentTrackPos = simTrack,
            currentHomeStep = simHome,
            steps = 1,
            isClassic = isClassic
        ) ?: break

        simState = nextStep.newState
        simTrack = nextStep.newTrackPos
        simHome = nextStep.newHomeStep

        steps.add(
            GameBoardConstants.getTokenGridCoord(
                nextToken.copy(state = simState, trackPos = simTrack, homeStep = simHome)
            )
        )

        if (simState == nextToken.state &&
            simTrack == nextToken.trackPos &&
            simHome == nextToken.homeStep
        ) {
            break
        }
    }

    if (steps.isEmpty()) {
        return listOf(GameBoardConstants.getTokenGridCoord(nextToken))
    }
    return steps
}

/**
 * Premium 3D Token Canvas Composable rendering 4 distinct metallic & glossy player designs:
 * - GREEN: Emerald Royal Shield
 * - YELLOW: Golden Royal Crown
 * - RED: Ruby Flame
 * - BLUE: Sapphire Crystal
 */
@Composable
fun Premium3DTokenCanvas(
    token: Token,
    isSelectable: Boolean,
    isMoving: Boolean,
    modifier: Modifier = Modifier,
    shimmerOverride: Float? = null,
    glowPulseOverride: Float? = null
) {
    val shimmer: Float
    val glowPulse: Float
    if (shimmerOverride != null && glowPulseOverride != null) {
        shimmer = shimmerOverride
        glowPulse = glowPulseOverride
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "token3d_${token.side}_${token.id}")
        val animatedShimmer by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "tokenShimmer"
        )
        val animatedGlow by infiniteTransition.animateFloat(
            initialValue = 0.65f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(750, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "tokenGlow"
        )
        shimmer = shimmerOverride ?: animatedShimmer
        glowPulse = glowPulseOverride ?: animatedGlow
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension / 2f

        // 1. Ambient Soft Drop Shadow underneath token
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.62f), Color.Transparent),
                center = Offset(cx, cy + r * 0.14f),
                radius = r * 1.04f
            ),
            radius = r * 1.04f,
            center = Offset(cx, cy + r * 0.14f)
        )

        // 2. Colored Rim Aura (amplified when selectable or moving)
        val auraColor = when (token.side) {
            PlayerSide.GREEN -> Color(0xFF34D399)
            PlayerSide.YELLOW -> Color(0xFFFDE047)
            PlayerSide.RED -> Color(0xFFFB7185)
            PlayerSide.BLUE -> Color(0xFF60A5FA)
        }
        val auraIntensity = if (isSelectable) 0.72f * glowPulse else if (isMoving) 0.62f else 0.28f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(auraColor.copy(alpha = auraIntensity), Color.Transparent),
                center = Offset(cx, cy),
                radius = r * 1.18f
            ),
            radius = r * 1.18f,
            center = Offset(cx, cy)
        )

        // 3. Draw Player-Specific 3D Sculpture
        when (token.side) {
            PlayerSide.GREEN -> drawEmeraldRoyalShield(cx, cy, r * 0.86f, shimmer, isSelectable)
            PlayerSide.YELLOW -> drawGoldenRoyalCrown(cx, cy, r * 0.86f, shimmer, isSelectable)
            PlayerSide.RED -> drawRubyFlame(cx, cy, r * 0.86f, shimmer, isSelectable)
            PlayerSide.BLUE -> drawSapphireCrystal(cx, cy, r * 0.86f, shimmer, isSelectable)
        }

        // 4. Shield Power-Up Outer Protective Bubble (Blitz mode)
        if (token.hasShield) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                    center = Offset(cx, cy),
                    radius = r * 0.96f
                ),
                radius = r * 0.96f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = Color(0xFF7DD3FC),
                radius = r * 0.96f,
                center = Offset(cx, cy),
                style = Stroke(width = (r * 0.10f).coerceAtLeast(1.8f))
            )
        }
    }
}

/**
 * 1. GREEN – Emerald Royal Shield
 */
private fun DrawScope.drawEmeraldRoyalShield(
    cx: Float,
    cy: Float,
    r: Float,
    shimmer: Float,
    isSelectable: Boolean
) {
    val shieldPath = Path().apply {
        moveTo(cx, cy - r * 0.95f)
        lineTo(cx + r * 0.86f, cy - r * 0.62f)
        cubicTo(
            cx + r * 0.88f, cy + r * 0.12f,
            cx + r * 0.56f, cy + r * 0.68f,
            cx, cy + r * 0.98f
        )
        cubicTo(
            cx - r * 0.56f, cy + r * 0.68f,
            cx - r * 0.88f, cy + r * 0.12f,
            cx - r * 0.86f, cy - r * 0.62f
        )
        close()
    }

    // 3D Metallic Gold Bevel Base (offset slightly down for thickness)
    withTransform({ translate(0f, r * 0.08f) }) {
        drawPath(
            path = shieldPath,
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF92400E), Color(0xFF451A03)),
                start = Offset(cx - r, cy - r),
                end = Offset(cx + r, cy + r)
            )
        )
    }

    // Outer Gold Rim
    drawPath(
        path = shieldPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFB45309)),
            start = Offset(cx - r * 0.7f, cy - r),
            end = Offset(cx + r * 0.7f, cy + r)
        )
    )

    // Inner Emerald Glossy Faceted Plate
    val innerPath = Path().apply {
        moveTo(cx, cy - r * 0.78f)
        lineTo(cx + r * 0.68f, cy - r * 0.50f)
        cubicTo(
            cx + r * 0.70f, cy + r * 0.08f,
            cx + r * 0.44f, cy + r * 0.54f,
            cx, cy + r * 0.78f
        )
        cubicTo(
            cx - r * 0.44f, cy + r * 0.54f,
            cx - r * 0.70f, cy + r * 0.08f,
            cx - r * 0.68f, cy - r * 0.50f
        )
        close()
    }
    drawPath(
        path = innerPath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF6EE7B7), Color(0xFF10B981), Color(0xFF047857), Color(0xFF022C22)),
            center = Offset(cx - r * 0.22f, cy - r * 0.25f),
            radius = r * 1.25f
        )
    )

    // Left glossy bevel facet highlight
    val leftFacet = Path().apply {
        moveTo(cx, cy - r * 0.78f)
        lineTo(cx, cy + r * 0.78f)
        cubicTo(
            cx - r * 0.44f, cy + r * 0.54f,
            cx - r * 0.70f, cy + r * 0.08f,
            cx - r * 0.68f, cy - r * 0.50f
        )
        close()
    }
    drawPath(
        path = leftFacet,
        color = Color.White.copy(alpha = 0.16f)
    )

    // Royal Gold Emblem Chevron & Gem inside Shield
    val crestPath = Path().apply {
        moveTo(cx, cy - r * 0.36f)
        lineTo(cx + r * 0.28f, cy + r * 0.04f)
        lineTo(cx, cy + r * 0.40f)
        lineTo(cx - r * 0.28f, cy + r * 0.04f)
        close()
    }
    drawPath(
        path = crestPath,
        brush = Brush.linearGradient(listOf(Color(0xFFFEF08A), Color(0xFFF59E0B)))
    )
    drawPath(
        path = crestPath,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(width = (r * 0.07f).coerceAtLeast(1f))
    )

    // Crisp rim outline
    drawPath(
        path = shieldPath,
        color = if (isSelectable) Color(0xFFFDE047) else Color(0xFFFEF3C7).copy(alpha = 0.9f),
        style = Stroke(width = (r * 0.10f).coerceAtLeast(1.4f), join = StrokeJoin.Round)
    )

    // Top-left specular highlight
    val glintPos = Offset(cx - r * 0.28f + sin(shimmer * 2f * PI.toFloat()) * r * 0.08f, cy - r * 0.38f)
    drawCircle(
        brush = Brush.radialGradient(listOf(Color.White.copy(alpha = 0.85f), Color.Transparent), center = glintPos, radius = r * 0.26f),
        radius = r * 0.26f,
        center = glintPos
    )
}

/**
 * 2. YELLOW – Golden Royal Crown
 */
private fun DrawScope.drawGoldenRoyalCrown(
    cx: Float,
    cy: Float,
    r: Float,
    shimmer: Float,
    isSelectable: Boolean
) {
    // Rich Dark Royal Medallion Base for contrast & 3D depth
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF78350F), Color(0xFF451A03), Color(0xFF1C0A00)),
            center = Offset(cx, cy),
            radius = r * 0.98f
        ),
        radius = r * 0.98f,
        center = Offset(cx, cy)
    )
    drawCircle(
        brush = Brush.linearGradient(listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFB45309))),
        radius = r * 0.98f,
        center = Offset(cx, cy),
        style = Stroke(width = (r * 0.14f).coerceAtLeast(1.8f))
    )

    // Crown Silhouette
    val crownPath = Path().apply {
        moveTo(cx - r * 0.66f, cy + r * 0.42f)
        lineTo(cx - r * 0.74f, cy - r * 0.36f)
        lineTo(cx - r * 0.32f, cy + r * 0.02f)
        lineTo(cx, cy - r * 0.62f)
        lineTo(cx + r * 0.32f, cy + r * 0.02f)
        lineTo(cx + r * 0.74f, cy - r * 0.36f)
        lineTo(cx + r * 0.66f, cy + r * 0.42f)
        close()
    }

    // 3D Crown Shadow Layer
    withTransform({ translate(0f, r * 0.08f) }) {
        drawPath(crownPath, color = Color(0xFF451A03))
    }

    // Metallic Gold Crown Body
    drawPath(
        path = crownPath,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFF59E0B), Color(0xFFB45309)),
            start = Offset(cx - r * 0.6f, cy - r * 0.6f),
            end = Offset(cx + r * 0.6f, cy + r * 0.5f)
        )
    )
    drawPath(
        path = crownPath,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(width = (r * 0.07f).coerceAtLeast(1f), join = StrokeJoin.Round)
    )

    // Crown Base Band
    drawRoundRect(
        brush = Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFFEF08A), Color(0xFFD97706))),
        topLeft = Offset(cx - r * 0.66f, cy + r * 0.24f),
        size = Size(r * 1.32f, r * 0.22f),
        cornerRadius = CornerRadius(r * 0.1f, r * 0.1f)
    )

    // Three Crown Tip Jewels (Ruby, Sapphire, Emerald)
    val tips = listOf(
        Triple(Offset(cx - r * 0.72f, cy - r * 0.38f), Color(0xFF38BDF8), r * 0.14f),
        Triple(Offset(cx, cy - r * 0.64f), Color(0xFFF43F5E), r * 0.18f),
        Triple(Offset(cx + r * 0.72f, cy - r * 0.38f), Color(0xFF10B981), r * 0.14f)
    )
    for ((pos, gemColor, gemR) in tips) {
        drawCircle(color = Color(0xFFFEF08A), radius = gemR * 1.25f, center = pos)
        drawCircle(
            brush = Brush.radialGradient(listOf(Color.White, gemColor), center = Offset(pos.x - gemR * 0.3f, pos.y - gemR * 0.3f), radius = gemR * 1.3f),
            radius = gemR,
            center = pos
        )
    }

    // Center Crown Ruby
    drawCircle(
        brush = Brush.radialGradient(listOf(Color(0xFFFFE4E6), Color(0xFFE11D48), Color(0xFF881337))),
        radius = r * 0.14f,
        center = Offset(cx, cy + r * 0.10f)
    )

    if (isSelectable) {
        drawCircle(
            color = Color.White,
            radius = r * 1.02f,
            center = Offset(cx, cy),
            style = Stroke(width = (r * 0.08f).coerceAtLeast(1.4f))
        )
    }

    // Shimmer sparkle on center crown peak
    val glintAlpha = (0.4f + 0.6f * absSin(shimmer * PI.toFloat()))
    drawCircle(
        color = Color.White.copy(alpha = glintAlpha),
        radius = r * 0.13f,
        center = Offset(cx - r * 0.16f, cy - r * 0.34f)
    )
}

/**
 * 3. RED – Ruby Flame
 */
private fun DrawScope.drawRubyFlame(
    cx: Float,
    cy: Float,
    r: Float,
    shimmer: Float,
    isSelectable: Boolean
) {
    // Beveled Crimson-Gold Base Disc
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF9F1239), Color(0xFF4C0519)),
            center = Offset(cx, cy),
            radius = r * 0.95f
        ),
        radius = r * 0.95f,
        center = Offset(cx, cy)
    )
    drawCircle(
        brush = Brush.linearGradient(listOf(Color(0xFFFFD1DC), Color(0xFFF43F5E), Color(0xFF881337))),
        radius = r * 0.95f,
        center = Offset(cx, cy),
        style = Stroke(width = (r * 0.14f).coerceAtLeast(1.8f))
    )

    // Outer Blazing Ruby Flame Silhouette
    val outerFlame = Path().apply {
        moveTo(cx, cy - r * 0.92f)
        cubicTo(
            cx + r * 0.42f, cy - r * 0.46f,
            cx + r * 0.82f, cy - r * 0.06f,
            cx + r * 0.68f, cy + r * 0.44f
        )
        cubicTo(
            cx + r * 0.56f, cy + r * 0.78f,
            cx + r * 0.24f, cy + r * 0.88f,
            cx, cy + r * 0.88f
        )
        cubicTo(
            cx - r * 0.24f, cy + r * 0.88f,
            cx - r * 0.56f, cy + r * 0.78f,
            cx - r * 0.68f, cy + r * 0.44f
        )
        cubicTo(
            cx - r * 0.82f, cy - r * 0.06f,
            cx - r * 0.36f, cy - r * 0.42f,
            cx, cy - r * 0.92f
        )
        close()
    }

    drawPath(
        path = outerFlame,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF88A5), Color(0xFFF43F5E), Color(0xFFBE123C), Color(0xFF881337))
        )
    )

    // Inner Glowing Amber-Gold Flame Tongue
    val innerFlame = Path().apply {
        moveTo(cx + r * 0.04f, cy - r * 0.46f)
        cubicTo(
            cx + r * 0.36f, cy - r * 0.08f,
            cx + r * 0.44f, cy + r * 0.32f,
            cx, cy + r * 0.68f
        )
        cubicTo(
            cx - r * 0.44f, cy + r * 0.32f,
            cx - r * 0.32f, cy - r * 0.12f,
            cx + r * 0.04f, cy - r * 0.46f
        )
        close()
    }
    drawPath(
        path = innerFlame,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFBEB), Color(0xFFFDE047), Color(0xFFF97316))
        )
    )

    // Crisp Glossy Edge
    drawPath(
        path = outerFlame,
        color = if (isSelectable) Color(0xFFFDE047) else Color(0xFFFFE4E6).copy(alpha = 0.9f),
        style = Stroke(width = (r * 0.09f).coerceAtLeast(1.3f), join = StrokeJoin.Round)
    )

    // Animated Ember Sparkle
    val sparkY = cy - r * 0.50f - (shimmer * r * 0.18f)
    drawCircle(
        color = Color(0xFFFEF08A).copy(alpha = (1f - shimmer) * 0.9f),
        radius = r * 0.12f,
        center = Offset(cx - r * 0.18f, sparkY)
    )
}

/**
 * 4. BLUE – Sapphire Crystal
 */
private fun DrawScope.drawSapphireCrystal(
    cx: Float,
    cy: Float,
    r: Float,
    shimmer: Float,
    isSelectable: Boolean
) {
    // Brushed Silver-Chrome Metallic Bezel Ring
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1E3A8A), Color(0xFF0F172A)),
            center = Offset(cx, cy),
            radius = r * 0.96f
        ),
        radius = r * 0.96f,
        center = Offset(cx, cy)
    )
    drawCircle(
        brush = Brush.linearGradient(listOf(Color.White, Color(0xFF93C5FD), Color(0xFF1D4ED8))),
        radius = r * 0.96f,
        center = Offset(cx, cy),
        style = Stroke(width = (r * 0.13f).coerceAtLeast(1.8f))
    )

    // Hexagonal/Octahedral 3D Sapphire Crystal Geometry
    val top = Offset(cx, cy - r * 0.88f)
    val topR = Offset(cx + r * 0.68f, cy - r * 0.34f)
    val botR = Offset(cx + r * 0.68f, cy + r * 0.34f)
    val bot = Offset(cx, cy + r * 0.88f)
    val botL = Offset(cx - r * 0.68f, cy + r * 0.34f)
    val topL = Offset(cx - r * 0.68f, cy - r * 0.34f)

    val crystalBody = Path().apply {
        moveTo(top.x, top.y)
        lineTo(topR.x, topR.y)
        lineTo(botR.x, botR.y)
        lineTo(bot.x, bot.y)
        lineTo(botL.x, botL.y)
        lineTo(topL.x, topL.y)
        close()
    }
    drawPath(
        path = crystalBody,
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFFBAE6FD), Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF1E1B4B)),
            start = top,
            end = bot
        )
    )

    // Inner Bright Cyan Table Facet
    val tablePath = Path().apply {
        moveTo(cx, cy - r * 0.46f)
        lineTo(cx + r * 0.36f, cy - r * 0.16f)
        lineTo(cx + r * 0.36f, cy + r * 0.16f)
        lineTo(cx, cy + r * 0.46f)
        lineTo(cx - r * 0.36f, cy + r * 0.16f)
        lineTo(cx - r * 0.36f, cy - r * 0.16f)
        close()
    }
    drawPath(
        path = tablePath,
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFF0F9FF), Color(0xFF7DD3FC), Color(0xFF0284C7)),
            center = Offset(cx - r * 0.12f, cy - r * 0.14f),
            radius = r * 0.65f
        )
    )

    // Facet Ridge Lines for Authentic 3D Cut Crystal Depth
    val ridgeStroke = (r * 0.06f).coerceAtLeast(1f)
    val ridgeColor = Color(0xFFE0F2FE).copy(alpha = 0.85f)
    drawLine(ridgeColor, top, Offset(cx, cy - r * 0.46f), strokeWidth = ridgeStroke)
    drawLine(ridgeColor, topR, Offset(cx + r * 0.36f, cy - r * 0.16f), strokeWidth = ridgeStroke)
    drawLine(ridgeColor, botR, Offset(cx + r * 0.36f, cy + r * 0.16f), strokeWidth = ridgeStroke)
    drawLine(ridgeColor, bot, Offset(cx, cy + r * 0.46f), strokeWidth = ridgeStroke)
    drawLine(ridgeColor, botL, Offset(cx - r * 0.36f, cy + r * 0.16f), strokeWidth = ridgeStroke)
    drawLine(ridgeColor, topL, Offset(cx - r * 0.36f, cy - r * 0.16f), strokeWidth = ridgeStroke)

    drawPath(
        path = crystalBody,
        color = if (isSelectable) Color(0xFFFDE047) else Color.White.copy(alpha = 0.92f),
        style = Stroke(width = (r * 0.09f).coerceAtLeast(1.3f), join = StrokeJoin.Round)
    )

    // Animated Crystal Star Glint
    val glintSize = r * (0.14f + 0.10f * absSin(shimmer * PI.toFloat()))
    val gx = cx - r * 0.22f
    val gy = cy - r * 0.26f
    drawLine(Color.White, Offset(gx - glintSize, gy), Offset(gx + glintSize, gy), strokeWidth = 2f, cap = StrokeCap.Round)
    drawLine(Color.White, Offset(gx, gy - glintSize), Offset(gx, gy + glintSize), strokeWidth = 2f, cap = StrokeCap.Round)
}

private fun absSin(v: Float): Float {
    val s = sin(v)
    return if (s < 0f) -s else s
}

/**
 * Board VFX event types for movement trails, capture explosions, and home-entry celebrations.
 */
enum class BoardVfxType {
    STEP_TRAIL,
    CAPTURE_EXPLOSION,
    HOME_CELEBRATION
}

data class BoardVfxBurst(
    val id: Long,
    val gridCoord: GridCoord,
    val color: Color,
    val type: BoardVfxType
)
