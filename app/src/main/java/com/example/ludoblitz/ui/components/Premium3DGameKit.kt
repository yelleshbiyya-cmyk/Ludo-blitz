package com.example.ludoblitz.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludoblitz.model.ChestType
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

/**
 * Tactile 3D Arcade Push-Button with physical bottom ledge, top glass reflection strip,
 * metallic highlight border, and smooth spring press/release animation.
 *
 * Uses `drawBehind` for all 3D chrome layers so it wraps content width naturally when placed
 * inside a Row alongside `Modifier.weight(1f)` AND fills width cleanly when caller passes
 * `Modifier.fillMaxWidth()` or explicit sizing.
 */
@Composable
fun Game3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    topColor: Color = Color(0xFFFDE047),
    bottomColor: Color = Color(0xFFF59E0B),
    ledgeColor: Color = Color(0xFF92400E),
    borderColor: Color = Color.White.copy(alpha = 0.85f),
    cornerRadius: Dp = 18.dp,
    height: Dp = 50.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "btn3dScale"
    )
    val ledgeDepth = if (isPressed && enabled) 1.dp else 4.dp

    val effectiveTop = if (enabled) topColor else Color(0xFF64748B)
    val effectiveBottom = if (enabled) bottomColor else Color(0xFF475569)
    val effectiveLedge = if (enabled) ledgeColor else Color(0xFF334155)
    val effectiveBorder = if (enabled) borderColor else Color.White.copy(alpha = 0.25f)
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .height(height)
            .scale(pressScale)
            .shadow(
                elevation = if (enabled) 10.dp else 2.dp,
                shape = shape,
                ambientColor = effectiveBottom,
                spotColor = effectiveBottom
            )
            .clip(shape)
            .drawBehind {
                val radiusPx = cornerRadius.toPx()
                val ledgePx = ledgeDepth.toPx()
                val faceHeight = (size.height - ledgePx).coerceAtLeast(1f)
                val cr = CornerRadius(radiusPx, radiusPx)

                // 1. Physical bottom 3D bevel ledge
                drawRoundRect(
                    color = effectiveLedge,
                    topLeft = Offset.Zero,
                    size = size,
                    cornerRadius = cr
                )

                // 2. Raised glossy 3D face shifted upward above ledge
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(effectiveTop, effectiveBottom),
                        startY = 0f,
                        endY = faceHeight
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, faceHeight),
                    cornerRadius = cr
                )

                // 3. Top curved glass specular highlight
                val sheenInsetX = (6.dp.toPx()).coerceAtMost(size.width * 0.14f)
                val sheenInsetY = 2.5.dp.toPx()
                val sheenWidth = (size.width - sheenInsetX * 2f).coerceAtLeast(0f)
                val sheenHeight = (faceHeight * 0.42f).coerceAtLeast(0f)
                if (sheenWidth > 2f && sheenHeight > 2f) {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.42f), Color.Transparent),
                            startY = sheenInsetY,
                            endY = sheenInsetY + sheenHeight
                        ),
                        topLeft = Offset(sheenInsetX, sheenInsetY),
                        size = Size(sheenWidth, sheenHeight),
                        cornerRadius = CornerRadius(radiusPx * 0.85f, radiusPx * 0.85f)
                    )
                }

                // 4. Crisp metallic rim stroke
                val strokePx = 1.5.dp.toPx()
                drawRoundRect(
                    color = effectiveBorder,
                    topLeft = Offset(strokePx / 2f, strokePx / 2f),
                    size = Size(
                        (size.width - strokePx).coerceAtLeast(1f),
                        (faceHeight - strokePx).coerceAtLeast(1f)
                    ),
                    cornerRadius = cr,
                    style = Stroke(width = strokePx)
                )
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(bottom = ledgeDepth)
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * Glossy Layered 3D Game Panel with luminous sapphire-violet depth, metallic gold/jewel rim,
 * and Canvas-painted top specular sheen (zero layout width side-effects).
 */
@Composable
fun Game3DCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(Color(0xFF34459E), Color(0xFF212B6B)),
    borderColors: List<Color> = listOf(RoyalGoldLight.copy(alpha = 0.85f), Color(0xFF60A5FA).copy(alpha = 0.7f)),
    cornerRadius: Dp = 22.dp,
    shadowColor: Color = Color(0xFF1E3A8A),
    borderWidth: Dp = 1.8.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .shadow(14.dp, shape, ambientColor = shadowColor, spotColor = shadowColor)
            .clip(shape)
            .background(Brush.verticalGradient(gradientColors))
            .drawBehind {
                val sheenH = 26.dp.toPx().coerceAtMost(size.height * 0.45f)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                        startY = 0f,
                        endY = sheenH
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width, sheenH)
                )
            }
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(borderColors),
                shape = shape
            ),
        content = content
    )
}

/**
 * Cohesive Royal 3D Section Title Header used across all game screens.
 */
@Composable
fun Section3DHeader(
    title: String,
    badge: String? = null,
    accentColor: Color = RoyalGoldLight,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 18.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Brush.verticalGradient(listOf(accentColor, BentoAmber)))
            )
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 0.8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E1B4B).copy(alpha = 0.55f))
                    .border(1.dp, accentColor.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/**
 * Maps an equipped profile frame ID to vibrant metallic jewel ring colors.
 */
fun profileFrameRingColors(frameId: String): List<Color> = when (frameId) {
    "frame_sapphire" -> listOf(Color(0xFFBAE6FD), Color(0xFF00E5FF), Color(0xFF1D4ED8))
    "frame_emerald" -> listOf(Color(0xFFA7F3D0), BentoEmerald, Color(0xFF065F46))
    "frame_ruby" -> listOf(Color(0xFFFDA4AF), BentoRose, Color(0xFF9F1239))
    "frame_mythic" -> listOf(RoyalGoldLight, Color(0xFFC084FC), BentoSky, BentoRose)
    else -> listOf(RoyalGoldLight, BentoAmber, RoyalGoldDark)
}

/**
 * Custom Canvas-rendered 3D Treasure Chest (Wooden, Silver, Gold, Blitz) with arched 3D lid,
 * metallic trim bands, glowing lock clasp, specular highlights, and aura glow.
 */
@Composable
fun Game3DChestCanvas(
    chestType: ChestType,
    size: Dp = 54.dp,
    modifier: Modifier = Modifier
) {
    val (bodyTop, bodyBot, bandLight, bandDark, glowColor) = when (chestType) {
        ChestType.WOODEN -> Quint(
            Color(0xFFD97706), Color(0xFF78350F),
            Color(0xFFFDE047), Color(0xFFB45309),
            Color(0xFFF59E0B)
        )
        ChestType.SILVER -> Quint(
            Color(0xFFE2E8F0), Color(0xFF475569),
            Color(0xFF7DD3FC), Color(0xFF0284C7),
            Color(0xFF38BDF8)
        )
        ChestType.GOLD -> Quint(
            Color(0xFFFEF08A), Color(0xFFB45309),
            Color(0xFFFFFFFF), Color(0xFFEAB308),
            Color(0xFFFACC15)
        )
        ChestType.BLITZ -> Quint(
            Color(0xFFC084FC), Color(0xFF581C87),
            Color(0xFF67E8F9), Color(0xFF0284C7),
            Color(0xFF22D3EE)
        )
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glowColor.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(cx, cy),
                radius = w * 0.56f
            ),
            radius = w * 0.56f,
            center = Offset(cx, cy)
        )

        drawOval(
            color = Color.Black.copy(alpha = 0.35f),
            topLeft = Offset(w * 0.14f, h * 0.78f),
            size = Size(w * 0.72f, h * 0.16f)
        )

        val chestLeft = w * 0.14f
        val chestWidth = w * 0.72f
        val lidTop = h * 0.20f
        val lidHeight = h * 0.26f
        val baseTop = h * 0.44f
        val baseHeight = h * 0.38f

        drawRoundRect(
            brush = Brush.verticalGradient(listOf(bodyTop, bodyBot)),
            topLeft = Offset(chestLeft, baseTop),
            size = Size(chestWidth, baseHeight),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
        )

        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.65f), bodyTop, bodyBot)),
            topLeft = Offset(chestLeft - w * 0.02f, lidTop),
            size = Size(chestWidth + w * 0.04f, lidHeight + h * 0.04f),
            cornerRadius = CornerRadius(w * 0.14f, w * 0.14f)
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.38f),
            topLeft = Offset(chestLeft + w * 0.06f, lidTop + h * 0.04f),
            size = Size(chestWidth - w * 0.12f, h * 0.06f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        val strapW = w * 0.10f
        val leftStrapX = chestLeft + chestWidth * 0.14f
        val rightStrapX = chestLeft + chestWidth * 0.76f
        for (sx in listOf(leftStrapX, rightStrapX)) {
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(bandLight, bandDark)),
                topLeft = Offset(sx, lidTop),
                size = Size(strapW, lidHeight + baseHeight + h * 0.02f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }

        drawRect(
            brush = Brush.horizontalGradient(listOf(bandDark, bandLight, bandDark)),
            topLeft = Offset(chestLeft - w * 0.02f, baseTop - h * 0.02f),
            size = Size(chestWidth + w * 0.04f, h * 0.07f)
        )

        val claspW = w * 0.18f
        val claspH = h * 0.19f
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(RoyalGoldLight, RoyalGoldDark)),
            topLeft = Offset(cx - claspW / 2f, baseTop - h * 0.05f),
            size = Size(claspW, claspH),
            cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
        )
        drawCircle(
            color = if (chestType == ChestType.BLITZ) Color(0xFF22D3EE) else Color(0xFF1E1B4B),
            radius = w * 0.036f,
            center = Offset(cx, baseTop + h * 0.045f)
        )

        drawRoundRect(
            color = bandLight.copy(alpha = 0.85f),
            topLeft = Offset(chestLeft, baseTop),
            size = Size(chestWidth, baseHeight),
            cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
            style = Stroke(width = 1.6f)
        )
    }
}

/**
 * Sculpted 3D Golden Trophy Icon rendered with Compose Canvas.
 */
@Composable
fun Game3DTrophyCanvas(
    size: Dp = 26.dp,
    primaryColor: Color = RoyalGold,
    darkColor: Color = RoyalGoldDark,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f

        drawArc(
            color = primaryColor,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.10f, h * 0.18f),
            size = Size(w * 0.28f, h * 0.34f),
            style = Stroke(width = (w * 0.09f).coerceAtLeast(2f), cap = StrokeCap.Round)
        )
        drawArc(
            color = darkColor,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.62f, h * 0.18f),
            size = Size(w * 0.28f, h * 0.34f),
            style = Stroke(width = (w * 0.09f).coerceAtLeast(2f), cap = StrokeCap.Round)
        )

        val cupPath = Path().apply {
            moveTo(w * 0.22f, h * 0.14f)
            lineTo(w * 0.78f, h * 0.14f)
            quadraticTo(w * 0.76f, h * 0.58f, cx, h * 0.62f)
            quadraticTo(w * 0.24f, h * 0.58f, w * 0.22f, h * 0.14f)
            close()
        }
        drawPath(
            path = cupPath,
            brush = Brush.horizontalGradient(listOf(RoyalGoldLight, primaryColor, darkColor))
        )
        drawPath(
            path = cupPath,
            color = Color.White.copy(alpha = 0.65f),
            style = Stroke(width = 1.2f)
        )

        drawRect(
            brush = Brush.horizontalGradient(listOf(RoyalGoldLight, darkColor)),
            topLeft = Offset(cx - w * 0.08f, h * 0.60f),
            size = Size(w * 0.16f, h * 0.18f)
        )

        drawRoundRect(
            brush = Brush.verticalGradient(listOf(primaryColor, Color(0xFF78350F))),
            topLeft = Offset(w * 0.24f, h * 0.76f),
            size = Size(w * 0.52f, h * 0.16f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = w * 0.07f,
            center = Offset(w * 0.38f, h * 0.28f)
        )
    }
}

/**
 * Glossy 3D Player Avatar Medallion with metallic jewel frame and cleanly positioned level badge
 * that never obscures the avatar icon even at compact header sizes.
 */
@Composable
fun Game3DAvatarBadge(
    emoji: String = "🦸",
    level: Int? = null,
    size: Dp = 44.dp,
    ringColors: List<Color> = listOf(RoyalGoldLight, BentoAmber, RoyalGoldDark),
    bgColors: List<Color> = listOf(Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF1E1B4B)),
    modifier: Modifier = Modifier
) {
    val isCompact = size <= 44.dp
    Box(
        modifier = modifier.size(width = size, height = if (level != null) size + 6.dp else size),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .shadow(8.dp, CircleShape, ambientColor = ringColors.first())
                .clip(CircleShape)
                .background(Brush.radialGradient(bgColors))
                .border(if (isCompact) 2.2.dp else 3.dp, Brush.linearGradient(ringColors), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.75f)
                    .height(size * 0.34f)
                    .padding(top = 2.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.38f), Color.Transparent)
                        )
                    )
            )
            Text(
                text = emoji,
                fontSize = (size.value * (if (isCompact) 0.44f else 0.46f)).sp,
                modifier = Modifier.offset(y = if (level != null && isCompact) (-1).dp else 0.dp)
            )
        }
        if (level != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(7.dp))
                    .background(Brush.horizontalGradient(listOf(RoyalGoldLight, BentoAmber)))
                    .border(1.dp, Color.White, RoundedCornerShape(7.dp))
                    .padding(horizontal = if (isCompact) 4.dp else 7.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "LV$level",
                    fontSize = if (isCompact) 7.5.sp else 9.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E1B4B),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

private data class Quint<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
