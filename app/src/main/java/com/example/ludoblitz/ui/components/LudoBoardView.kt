package com.example.ludoblitz.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ludoblitz.model.GameBoardConstants
import com.example.ludoblitz.model.GridCoord
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.model.Token
import com.example.ludoblitz.model.TokenState
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoSky
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

// Vivid Royal 3D Ludo Quadrant Colors (High Contrast, Bright & Jewel-Rich)
private val LudoRed = Color(0xFFFF2E55)
private val LudoRedLight = Color(0xFFFF6B8B)
private val LudoRedDark = Color(0xFFBE123C)

private val LudoYellow = Color(0xFFFFC107)
private val LudoYellowLight = Color(0xFFFEF08A)
private val LudoYellowDark = Color(0xFFB45309)

private val LudoGreen = Color(0xFF10B981)
private val LudoGreenLight = Color(0xFF6EE7B7)
private val LudoGreenDark = Color(0xFF047857)

private val LudoBlue = Color(0xFF2563EB)
private val LudoBlueLight = Color(0xFF60A5FA)
private val LudoBlueDark = Color(0xFF1E3A8A)

// Bright Glossy Ivory-Pearl 3D Track Tiles (makes tokens, stars & colored lanes POP!)
private val TrackTileLight = Color(0xFFFFFFFF)
private val TrackTileBase = Color(0xFFF1F5F9)
private val TrackTileAlt = Color(0xFFE2E8F0)
private val TrackTileGroove = Color(0xFF94A3B8)
private val StarGold = Color(0xFFFACC15)
private val StarGoldBright = Color(0xFFFEF08A)
private val StarGoldDeep = Color(0xFFB45309)

private data class MovePreviewHint(
    val fromCoord: GridCoord,
    val destCoord: GridCoord,
    val isCapture: Boolean,
    val isFinish: Boolean,
    val isSafe: Boolean
)

/**
 * Premium 3D Traditional 15x15 Ludo Board with:
 * - Metallic Royal Gold outer frame with active-player glowing rim
 * - Bright Ivory-Pearl raised glossy 3D tiles with crisp bevel lighting
 * - Sculpted 3D corner Home Bases with snow-white yards & recessed circular pawn pedestals
 * - 3D Beveled Golden Safe Stars & Sculpted Metallic Gold Center Victory Crown
 * - Active Player Quadrant Spotlight & Best-Move Destination Preview Rings
 * - Fluid continuous path glide animation & 3D tokens
 */
@Composable
fun LudoBoardView(
    playerTokens: List<Token> = emptyList(),
    opponentTokens: List<Token> = emptyList(),
    allTokensMap: Map<PlayerSide, List<Token>>? = null,
    selectableTokenIds: Set<Int>,
    currentTurn: PlayerSide,
    diceValue: Int = 1,
    hasRolled: Boolean = false,
    isClassic: Boolean = false,
    onTokenClicked: (tokenId: Int) -> Unit,
    onTokenStepSound: (stepIndex: Int, totalSteps: Int) -> Unit = { _, _ -> },
    onTokenLandSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "boardPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tokenPulse"
    )
    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "haloRotation"
    )
    val starGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starGlow"
    )
    val sharedTokenShimmer = haloRotation / 360f
    val sharedTokenGlow = 0.65f + (starGlow - 0.65f)

    val rawTurnColor = when (currentTurn) {
        PlayerSide.RED -> LudoRed
        PlayerSide.GREEN -> LudoGreen
        PlayerSide.YELLOW -> LudoYellow
        PlayerSide.BLUE -> LudoBlue
    }
    val turnBorderColor by animateColorAsState(rawTurnColor, tween(280), label = "boardTurnColor")

    val vfxBursts = remember { mutableStateListOf<BoardVfxBurst>() }
    fun addVfx(burst: BoardVfxBurst) {
        if (vfxBursts.size >= 10) {
            vfxBursts.removeAt(0)
        }
        vfxBursts.add(burst)
    }

    val tokensToDraw = allTokensMap?.values?.flatten() ?: (playerTokens + opponentTokens)

    val movePreviewHints = remember(tokensToDraw, selectableTokenIds, currentTurn, diceValue, hasRolled, isClassic) {
        if (!hasRolled || selectableTokenIds.isEmpty()) {
            emptyList()
        } else {
            val activeTokens = tokensToDraw.filter { it.side == currentTurn && selectableTokenIds.contains(it.id) }
            val otherTokens = tokensToDraw.filter { it.side != currentTurn }
            activeTokens.mapNotNull { token ->
                val move = GameBoardConstants.calculateMove(
                    side = currentTurn,
                    currentState = token.state,
                    currentTrackPos = token.trackPos,
                    currentHomeStep = token.homeStep,
                    steps = diceValue,
                    isClassic = isClassic
                ) ?: return@mapNotNull null

                val destToken = token.copy(
                    state = move.newState,
                    trackPos = move.newTrackPos,
                    homeStep = move.newHomeStep
                )
                val isCapture = move.newState == TokenState.TRACK &&
                    !GameBoardConstants.isSafeTile(move.newTrackPos) &&
                    otherTokens.any { it.state == TokenState.TRACK && it.trackPos == move.newTrackPos }
                val isFinish = move.newState == TokenState.FINISHED
                val isSafe = move.newState == TokenState.TRACK && GameBoardConstants.isSafeTile(move.newTrackPos)

                MovePreviewHint(
                    fromCoord = GameBoardConstants.getTokenGridCoord(token),
                    destCoord = GameBoardConstants.getTokenGridCoord(destToken),
                    isCapture = isCapture,
                    isFinish = isFinish,
                    isSafe = isSafe
                )
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = turnBorderColor,
                spotColor = StarGold
            )
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(StarGoldBright, StarGold, StarGoldDeep, StarGold)
                )
            )
            .border(
                width = 3.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(turnBorderColor, Color.White.copy(alpha = 0.85f * starGlow), turnBorderColor)
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(5.dp)
            .testTag("ludo_board_canvas")
    ) {
        val boardWidthPx = constraints.maxWidth.toFloat()
        val cellSizePx = boardWidthPx / 15f
        val density = LocalDensity.current

        // 1. Draw 3D Illuminated 15x15 Ludo Board + Active Spotlight + Best-Move Hints
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(19.dp))
        ) {
            drawTraditionalLudoBoard(cellSizePx, starGlow, currentTurn)
            if (movePreviewHints.isNotEmpty()) {
                drawMovePreviewHints(movePreviewHints, cellSizePx, turnBorderColor, starGlow)
            }
        }

        // 2. Overlay Smoothly Gliding 3D Tokens (preserving hitboxes & touch controls)
        val tokensByCoord = tokensToDraw.groupBy { token ->
            val coord = GameBoardConstants.getTokenGridCoord(token)
            "${coord.row.toInt()}_${coord.col.toInt()}_${token.state}"
        }

        for (token in tokensToDraw) {
            androidx.compose.runtime.key(token.side, token.id) {
                val isSelectable = token.side == currentTurn && selectableTokenIds.contains(token.id)
                val targetCoord = GameBoardConstants.getTokenGridCoord(token)

                val group = tokensByCoord["${targetCoord.row.toInt()}_${targetCoord.col.toInt()}_${token.state}"] ?: listOf(token)
                val indexInGroup = group.indexOf(token)
                val groupOffsetGrid = if (group.size > 1 && token.state != TokenState.BASE && token.state != TokenState.FINISHED) {
                    val offsetAngle = (indexInGroup.toDouble() / group.size.toDouble()) * 2 * Math.PI
                    GridCoord(
                        row = (sin(offsetAngle) * 0.24).toFloat(),
                        col = (cos(offsetAngle) * 0.24).toFloat()
                    )
                } else {
                    GridCoord(0f, 0f)
                }

                val tokenColor = when (token.side) {
                    PlayerSide.GREEN -> LudoGreen
                    PlayerSide.YELLOW -> LudoYellow
                    PlayerSide.RED -> LudoRed
                    PlayerSide.BLUE -> LudoBlue
                }

                var prevTokenSnapshot by remember(token.side, token.id) { mutableStateOf(token) }
                val visualRow = remember(token.side, token.id) { Animatable(targetCoord.row + groupOffsetGrid.row) }
                val visualCol = remember(token.side, token.id) { Animatable(targetCoord.col + groupOffsetGrid.col) }
                val hopScale = remember(token.side, token.id) { Animatable(1f) }
                val moveTilt = remember(token.side, token.id) { Animatable(0f) }
                val glideProgress = remember(token.side, token.id) { Animatable(0f) }
                var isMovingNow by remember(token.side, token.id) { mutableStateOf(false) }

                LaunchedEffect(token.state, token.trackPos, token.homeStep, groupOffsetGrid.row, groupOffsetGrid.col) {
                    val prev = prevTokenSnapshot
                    val posChanged = prev.state != token.state ||
                        prev.trackPos != token.trackPos ||
                        prev.homeStep != token.homeStep

                    if (posChanged) {
                        val wasCaptured = token.state == TokenState.BASE && prev.state != TokenState.BASE
                        val enteredHome = token.state == TokenState.FINISHED && prev.state != TokenState.FINISHED
                        val startCoord = GridCoord(visualRow.value, visualCol.value)

                        if (wasCaptured) {
                            val captureCoord = GameBoardConstants.getTokenGridCoord(prev)
                            addVfx(
                                BoardVfxBurst(
                                    id = System.nanoTime(),
                                    gridCoord = captureCoord,
                                    color = tokenColor,
                                    type = BoardVfxType.CAPTURE_EXPLOSION
                                )
                            )
                        }

                        val rawSteps = computeTokenStepPath(prev, token, isClassic)
                        val finalCoord = GridCoord(
                            row = targetCoord.row + groupOffsetGrid.row,
                            col = targetCoord.col + groupOffsetGrid.col
                        )
                        val waypoints = buildList {
                            add(startCoord)
                            if (rawSteps.size > 1) {
                                for (i in 0 until rawSteps.lastIndex) {
                                    add(rawSteps[i])
                                }
                            }
                            add(finalCoord)
                        }

                        isMovingNow = true

                        val totalDurationMs = when {
                            wasCaptured -> 520
                            waypoints.size <= 2 -> 380
                            else -> (360 + (waypoints.size - 1) * 58).coerceIn(420, 740)
                        }

                        val segments = (waypoints.size - 1).coerceAtLeast(1)
                        var lastSoundStep = -1

                        glideProgress.snapTo(0f)
                        glideProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = totalDurationMs, easing = FastOutSlowInEasing)
                        ) {
                            val t = value
                            val scaled = t * segments
                            val segIdx = floor(scaled).toInt().coerceIn(0, segments - 1)
                            val localT = (scaled - segIdx).coerceIn(0f, 1f)

                            val a = waypoints[segIdx]
                            val b = waypoints[segIdx + 1]
                            val curRow = a.row + (b.row - a.row) * localT
                            val curCol = a.col + (b.col - a.col) * localT

                            val arcLift = sin(t * PI.toFloat())
                            val scaleNow = 1f + (if (wasCaptured) 0.42f else 0.30f) * arcLift
                            val tiltNow = sin(t * PI.toFloat() * 2f) * 11f

                            launch { visualRow.snapTo(curRow) }
                            launch { visualCol.snapTo(curCol) }
                            launch { hopScale.snapTo(scaleNow) }
                            launch { moveTilt.snapTo(tiltNow) }

                            if (!wasCaptured && segIdx > lastSoundStep && segIdx < segments) {
                                lastSoundStep = segIdx
                                onTokenStepSound(segIdx, segments)
                                if (segIdx % 2 == 0) {
                                    addVfx(
                                        BoardVfxBurst(
                                            id = System.nanoTime() + segIdx,
                                            gridCoord = GridCoord(curRow, curCol),
                                            color = tokenColor,
                                            type = BoardVfxType.STEP_TRAIL
                                        )
                                    )
                                }
                            }
                        }

                        visualRow.snapTo(finalCoord.row)
                        visualCol.snapTo(finalCoord.col)
                        addVfx(
                            BoardVfxBurst(
                                id = System.nanoTime() + 77,
                                gridCoord = targetCoord,
                                color = tokenColor,
                                type = BoardVfxType.STEP_TRAIL
                            )
                        )
                        coroutineScope {
                            launch { moveTilt.animateTo(0f, tween(110)) }
                            launch {
                                hopScale.snapTo(1.16f)
                                hopScale.animateTo(1.0f, tween(120, easing = FastOutSlowInEasing))
                            }
                        }
                        if (!wasCaptured) {
                            onTokenLandSound()
                        }

                        if (enteredHome) {
                            addVfx(
                                BoardVfxBurst(
                                    id = System.nanoTime() + 99,
                                    gridCoord = GridCoord(7f, 7f),
                                    color = StarGold,
                                    type = BoardVfxType.HOME_CELEBRATION
                                )
                            )
                        }

                        isMovingNow = false
                        prevTokenSnapshot = token
                    } else {
                        launch {
                            visualRow.animateTo(targetCoord.row + groupOffsetGrid.row, tween(150))
                        }
                        launch {
                            visualCol.animateTo(targetCoord.col + groupOffsetGrid.col, tween(150))
                        }
                    }
                }

                val centerX = (visualCol.value + 0.5f) * cellSizePx
                val centerY = (visualRow.value + 0.5f) * cellSizePx

                val hitboxSizePx = cellSizePx * 1.12f
                val hitboxSizeDp = with(density) { hitboxSizePx.toDp() }
                val halfHitboxPx = hitboxSizePx / 2f

                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (centerX - halfHitboxPx).roundToInt(),
                                (centerY - halfHitboxPx).roundToInt()
                            )
                        }
                        .size(hitboxSizeDp)
                        .scale((if (isSelectable) pulseScale else 1f) * hopScale.value)
                        .rotate(moveTilt.value)
                        .clickable(
                            enabled = isSelectable,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onTokenClicked(token.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelectable) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(1.08f)
                                .rotate(haloRotation)
                                .border(
                                    width = 2.6.dp,
                                    brush = Brush.sweepGradient(listOf(StarGoldBright, Color.White, tokenColor, StarGoldBright)),
                                    shape = CircleShape
                                )
                        )
                    }

                    Premium3DTokenCanvas(
                        token = token,
                        isSelectable = isSelectable,
                        isMoving = isMovingNow,
                        shimmerOverride = sharedTokenShimmer,
                        glowPulseOverride = sharedTokenGlow,
                        modifier = Modifier.fillMaxSize(0.92f)
                    )
                }
            }
        }

        // 3. Board Particle Effects Overlay
        for (burst in vfxBursts.toList()) {
            BoardVfxBurstView(
                burst = burst,
                cellSizePx = cellSizePx,
                onFinished = { vfxBursts.removeAll { it.id == burst.id } }
            )
        }
    }
}

private fun DrawScope.drawMovePreviewHints(
    hints: List<MovePreviewHint>,
    cellSize: Float,
    turnColor: Color,
    pulse: Float
) {
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    for (hint in hints) {
        val fromCenter = Offset((hint.fromCoord.col + 0.5f) * cellSize, (hint.fromCoord.row + 0.5f) * cellSize)
        val destCenter = Offset((hint.destCoord.col + 0.5f) * cellSize, (hint.destCoord.row + 0.5f) * cellSize)

        val ringColor = when {
            hint.isCapture -> Color(0xFFFF5500)
            hint.isFinish -> StarGoldDeep
            hint.isSafe -> Color(0xFF059669)
            else -> turnColor
        }

        drawLine(
            color = ringColor.copy(alpha = 0.65f * pulse),
            start = fromCenter,
            end = destCenter,
            strokeWidth = 2.6f,
            cap = StrokeCap.Round,
            pathEffect = dashEffect
        )

        val beaconRadius = cellSize * (0.42f + 0.12f * pulse)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(ringColor.copy(alpha = 0.52f * pulse), Color.Transparent),
                center = destCenter,
                radius = beaconRadius * 1.35f
            ),
            radius = beaconRadius * 1.35f,
            center = destCenter
        )
        drawCircle(
            color = ringColor,
            radius = cellSize * 0.36f,
            center = destCenter,
            style = Stroke(width = if (hint.isCapture || hint.isFinish) 3.4f else 2.6f)
        )
        drawCircle(
            color = if (hint.isCapture || hint.isFinish) StarGold else ringColor,
            radius = cellSize * (if (hint.isCapture || hint.isFinish) 0.16f else 0.12f),
            center = destCenter
        )
    }
}

@Composable
private fun BoardVfxBurstView(
    burst: BoardVfxBurst,
    cellSizePx: Float,
    onFinished: () -> Unit
) {
    val progress = remember(burst.id) { Animatable(0f) }
    val duration = when (burst.type) {
        BoardVfxType.STEP_TRAIL -> 340
        BoardVfxType.CAPTURE_EXPLOSION -> 680
        BoardVfxType.HOME_CELEBRATION -> 980
    }

    LaunchedEffect(burst.id) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(duration, easing = FastOutSlowInEasing))
        onFinished()
    }

    val p = progress.value
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = (burst.gridCoord.col + 0.5f) * cellSizePx
        val cy = (burst.gridCoord.row + 0.5f) * cellSizePx

        when (burst.type) {
            BoardVfxType.STEP_TRAIL -> {
                val alpha = (1f - p).coerceIn(0f, 1f)
                for (i in 0 until 6) {
                    val angle = (i / 6f) * 2f * PI.toFloat()
                    val dist = cellSizePx * (0.15f + 0.48f * p)
                    drawCircle(
                        color = burst.color.copy(alpha = alpha * 0.85f),
                        radius = (cellSizePx * 0.14f) * (1f - p * 0.6f),
                        center = Offset(cx + cos(angle) * dist, cy + sin(angle) * dist)
                    )
                }
            }
            BoardVfxType.CAPTURE_EXPLOSION -> {
                val alpha = (1f - p).coerceIn(0f, 1f)
                drawCircle(
                    color = Color(0xFFFDE047).copy(alpha = alpha),
                    radius = cellSizePx * (0.4f + 1.5f * p),
                    center = Offset(cx, cy),
                    style = Stroke(width = (6f * (1f - p)).coerceAtLeast(1f))
                )
                val colors = listOf(Color(0xFFFDE047), Color(0xFFF97316), Color(0xFFF43F5E), Color.White)
                for (i in 0 until 14) {
                    val angle = (i / 14f) * 2f * PI.toFloat()
                    val dist = cellSizePx * (0.2f + 1.4f * p)
                    drawCircle(
                        color = colors[i % colors.size].copy(alpha = alpha),
                        radius = cellSizePx * 0.18f * (1f - p * 0.5f),
                        center = Offset(cx + cos(angle) * dist, cy + sin(angle) * dist)
                    )
                }
            }
            BoardVfxType.HOME_CELEBRATION -> {
                val alpha = (1f - p).coerceIn(0f, 1f)
                val colors = listOf(StarGold, LudoGreen, LudoRed, LudoBlue, Color.White)
                drawCircle(
                    color = StarGold.copy(alpha = alpha * 0.85f),
                    radius = cellSizePx * (0.5f + 2.1f * p),
                    center = Offset(cx, cy),
                    style = Stroke(width = (7f * (1f - p)).coerceAtLeast(1.5f))
                )
                for (i in 0 until 18) {
                    val angle = (i / 18f) * 2f * PI.toFloat() + p * 1.4f
                    val dist = cellSizePx * (0.25f + 2.0f * p)
                    drawCircle(
                        color = colors[i % colors.size].copy(alpha = alpha),
                        radius = cellSizePx * 0.20f * (1f - p * 0.45f),
                        center = Offset(cx + cos(angle) * dist, cy + sin(angle) * dist)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTraditionalLudoBoard(
    cellSize: Float,
    starGlow: Float,
    activeTurn: PlayerSide
) {
    // Bright Pearl-White Sculpted Board Slab Background
    drawRect(color = Color(0xFFCBD5E1), size = size)

    // 1. Draw 4 Corner 3D Home Bases (6x6 cells each) — spotlight the ACTIVE player's quadrant!
    drawLudoBase(0f, 0f, 6 * cellSize, LudoGreenLight, LudoGreen, LudoGreenDark, cellSize, isActive = activeTurn == PlayerSide.GREEN, pulse = starGlow)
    drawLudoBase(9 * cellSize, 0f, 6 * cellSize, LudoYellowLight, LudoYellow, LudoYellowDark, cellSize, isActive = activeTurn == PlayerSide.YELLOW, pulse = starGlow)
    drawLudoBase(0f, 9 * cellSize, 6 * cellSize, LudoRedLight, LudoRed, LudoRedDark, cellSize, isActive = activeTurn == PlayerSide.RED, pulse = starGlow)
    drawLudoBase(9 * cellSize, 9 * cellSize, 6 * cellSize, LudoBlueLight, LudoBlue, LudoBlueDark, cellSize, isActive = activeTurn == PlayerSide.BLUE, pulse = starGlow)

    // 2. Draw Glossy Raised 3D Track Tiles
    // Top arm: rows 0..5, cols 6..8
    for (r in 0..5) {
        for (c in 6..8) {
            val isHomeLane = c == 7 && r in 1..5
            val isStart = r == 1 && c == 8
            val isStar = r == 2 && c == 6
            val (topC, botC) = when {
                isHomeLane || isStart -> LudoYellowLight to LudoYellow
                (r + c) % 2 == 0 -> TrackTileLight to TrackTileBase
                else -> TrackTileBase to TrackTileAlt
            }
            drawGlossy3DTile(c * cellSize, r * cellSize, cellSize, topC, botC, isColored = isHomeLane || isStart)
            if (isStar || isStart) draw3DSafeStar(c * cellSize + cellSize / 2f, r * cellSize + cellSize / 2f, cellSize * 0.35f, starGlow, onColoredTile = isStart)
        }
    }

    // Bottom arm: rows 9..14, cols 6..8
    for (r in 9..14) {
        for (c in 6..8) {
            val isHomeLane = c == 7 && r in 9..13
            val isStart = r == 13 && c == 6
            val isStar = r == 12 && c == 8
            val (topC, botC) = when {
                isHomeLane || isStart -> LudoRedLight to LudoRed
                (r + c) % 2 == 0 -> TrackTileLight to TrackTileBase
                else -> TrackTileBase to TrackTileAlt
            }
            drawGlossy3DTile(c * cellSize, r * cellSize, cellSize, topC, botC, isColored = isHomeLane || isStart)
            if (isStar || isStart) draw3DSafeStar(c * cellSize + cellSize / 2f, r * cellSize + cellSize / 2f, cellSize * 0.35f, starGlow, onColoredTile = isStart)
        }
    }

    // Left arm: rows 6..8, cols 0..5
    for (r in 6..8) {
        for (c in 0..5) {
            val isHomeLane = r == 7 && c in 1..5
            val isStart = r == 6 && c == 1
            val isStar = r == 8 && c == 2
            val (topC, botC) = when {
                isHomeLane || isStart -> LudoGreenLight to LudoGreen
                (r + c) % 2 == 0 -> TrackTileLight to TrackTileBase
                else -> TrackTileBase to TrackTileAlt
            }
            drawGlossy3DTile(c * cellSize, r * cellSize, cellSize, topC, botC, isColored = isHomeLane || isStart)
            if (isStar || isStart) draw3DSafeStar(c * cellSize + cellSize / 2f, r * cellSize + cellSize / 2f, cellSize * 0.35f, starGlow, onColoredTile = isStart)
        }
    }

    // Right arm: rows 6..8, cols 9..14
    for (r in 6..8) {
        for (c in 9..14) {
            val isHomeLane = r == 7 && c in 9..13
            val isStart = r == 8 && c == 13
            val isStar = r == 6 && c == 12
            val (topC, botC) = when {
                isHomeLane || isStart -> LudoBlueLight to LudoBlue
                (r + c) % 2 == 0 -> TrackTileLight to TrackTileBase
                else -> TrackTileBase to TrackTileAlt
            }
            drawGlossy3DTile(c * cellSize, r * cellSize, cellSize, topC, botC, isColored = isHomeLane || isStart)
            if (isStar || isStart) draw3DSafeStar(c * cellSize + cellSize / 2f, r * cellSize + cellSize / 2f, cellSize * 0.35f, starGlow, onColoredTile = isStart)
        }
    }

    // 3. Draw Metallic Gold Center Victory Goal (3x3 cells)
    val centerLeft = 6 * cellSize
    val centerTop = 6 * cellSize
    val centerSize = 3 * cellSize
    val centerMidX = 7.5f * cellSize
    val centerMidY = 7.5f * cellSize

    // Glossy 4-Color Victory Triangles
    val yellowPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerLeft + centerSize, centerTop)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(yellowPath, brush = Brush.verticalGradient(listOf(LudoYellowLight, LudoYellow, LudoYellowDark)))

    val redPath = Path().apply {
        moveTo(centerLeft, centerTop + centerSize)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(redPath, brush = Brush.verticalGradient(listOf(LudoRedDark, LudoRed, LudoRedLight)))

    val greenPath = Path().apply {
        moveTo(centerLeft, centerTop)
        lineTo(centerLeft, centerTop + centerSize)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(greenPath, brush = Brush.horizontalGradient(listOf(LudoGreenLight, LudoGreen, LudoGreenDark)))

    val bluePath = Path().apply {
        moveTo(centerLeft + centerSize, centerTop)
        lineTo(centerLeft + centerSize, centerTop + centerSize)
        lineTo(centerMidX, centerMidY)
        close()
    }
    drawPath(bluePath, brush = Brush.horizontalGradient(listOf(LudoBlueDark, LudoBlue, LudoBlueLight)))

    // Diagonal metallic gold separator seams across the center
    drawLine(
        color = StarGoldBright,
        start = Offset(centerLeft, centerTop),
        end = Offset(centerLeft + centerSize, centerTop + centerSize),
        strokeWidth = 2.6f
    )
    drawLine(
        color = StarGoldBright,
        start = Offset(centerLeft + centerSize, centerTop),
        end = Offset(centerLeft, centerTop + centerSize),
        strokeWidth = 2.6f
    )

    // Metallic Gold Frame around the 3x3 Center
    drawRect(
        brush = Brush.linearGradient(listOf(StarGoldBright, StarGold, StarGoldDeep, StarGoldBright)),
        topLeft = Offset(centerLeft, centerTop),
        size = Size(centerSize, centerSize),
        style = Stroke(width = 4.2f)
    )

    // Sculpted 3D Center Royal Crown Medallion
    val medalR = cellSize * 0.64f
    // Drop shadow
    drawCircle(
        color = Color.Black.copy(alpha = 0.38f),
        radius = medalR * 1.06f,
        center = Offset(centerMidX + 2f, centerMidY + 3f)
    )
    // Metallic Gold Outer Ring
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(StarGoldBright, StarGold, StarGoldDeep),
            start = Offset(centerMidX - medalR, centerMidY - medalR),
            end = Offset(centerMidX + medalR, centerMidY + medalR)
        ),
        radius = medalR,
        center = Offset(centerMidX, centerMidY)
    )
    // Inner Royal Sapphire Jewel Face
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF4338CA), Color(0xFF1E1B4B)),
            center = Offset(centerMidX - medalR * 0.2f, centerMidY - medalR * 0.2f),
            radius = medalR * 0.9f
        ),
        radius = medalR * 0.80f,
        center = Offset(centerMidX, centerMidY)
    )
    // Sculpted 3D Golden Royal Crown inside the center medallion!
    val cw = medalR * 1.05f
    val ch = medalR * 0.72f
    val crownBaseY = centerMidY + ch * 0.42f
    val crownTopY = centerMidY - ch * 0.48f
    val crownPath = Path().apply {
        moveTo(centerMidX - cw * 0.46f, crownBaseY)
        lineTo(centerMidX - cw * 0.52f, crownTopY + ch * 0.16f)
        lineTo(centerMidX - cw * 0.22f, centerMidY)
        lineTo(centerMidX, crownTopY - ch * 0.08f)
        lineTo(centerMidX + cw * 0.22f, centerMidY)
        lineTo(centerMidX + cw * 0.52f, crownTopY + ch * 0.16f)
        lineTo(centerMidX + cw * 0.46f, crownBaseY)
        close()
    }
    drawPath(
        path = crownPath,
        brush = Brush.verticalGradient(listOf(StarGoldBright, StarGold, StarGoldDeep))
    )
    drawPath(
        path = crownPath,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(width = 1.4f)
    )
    // Crown Jewel Tip Pearls
    drawCircle(color = Color.White, radius = medalR * 0.09f, center = Offset(centerMidX, crownTopY - ch * 0.10f))
    drawCircle(color = StarGoldBright, radius = medalR * 0.07f, center = Offset(centerMidX - cw * 0.50f, crownTopY + ch * 0.14f))
    drawCircle(color = StarGoldBright, radius = medalR * 0.07f, center = Offset(centerMidX + cw * 0.50f, crownTopY + ch * 0.14f))
}

private fun DrawScope.drawLudoBase(
    x: Float,
    y: Float,
    baseSize: Float,
    lightColor: Color,
    primaryColor: Color,
    darkColor: Color,
    cellSize: Float,
    isActive: Boolean,
    pulse: Float
) {
    // 3D Glossy Beveled Corner Quadrant
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(lightColor, primaryColor, darkColor),
            start = Offset(x, y),
            end = Offset(x + baseSize, y + baseSize)
        ),
        topLeft = Offset(x + 1.5f, y + 1.5f),
        size = Size(baseSize - 3f, baseSize - 3f),
        cornerRadius = CornerRadius(14f, 14f)
    )

    // Top specular glass highlight on colored quadrant frame
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White.copy(alpha = 0.34f), Color.Transparent),
            startY = y,
            endY = y + baseSize * 0.35f
        ),
        topLeft = Offset(x + 3f, y + 3f),
        size = Size(baseSize - 6f, baseSize * 0.35f),
        cornerRadius = CornerRadius(12f, 12f)
    )

    // Soft dimming for inactive corners so active player's corner glows unmistakably
    if (!isActive) {
        drawRoundRect(
            color = Color(0xFF0F172A).copy(alpha = 0.22f),
            topLeft = Offset(x + 1.5f, y + 1.5f),
            size = Size(baseSize - 3f, baseSize - 3f),
            cornerRadius = CornerRadius(14f, 14f)
        )
    } else {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(StarGoldBright.copy(alpha = 0.38f * pulse), Color.Transparent),
                center = Offset(x + baseSize / 2f, y + baseSize / 2f),
                radius = baseSize * 0.78f
            ),
            topLeft = Offset(x + 1.5f, y + 1.5f),
            size = Size(baseSize - 3f, baseSize - 3f),
            cornerRadius = CornerRadius(14f, 14f)
        )
    }

    // Active Pulsing Golden/White Quadrant Rim
    drawRoundRect(
        color = if (isActive) StarGoldBright.copy(alpha = 0.72f + 0.28f * pulse) else Color.White.copy(alpha = 0.45f),
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(baseSize - 4f, baseSize - 4f),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = if (isActive) 4.6f else 2f)
    )

    // Crisp Snow-White 3D Inner Yard with soft inner shadow
    val nestPadding = baseSize * 0.145f
    val nestSize = baseSize - (nestPadding * 2)
    drawRoundRect(
        color = darkColor.copy(alpha = 0.45f),
        topLeft = Offset(x + nestPadding + 2f, y + nestPadding + 3f),
        size = Size(nestSize, nestSize),
        cornerRadius = CornerRadius(18f, 18f)
    )
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFF1F5F9))
        ),
        topLeft = Offset(x + nestPadding, y + nestPadding),
        size = Size(nestSize, nestSize),
        cornerRadius = CornerRadius(18f, 18f)
    )
    drawRoundRect(
        color = if (isActive) StarGold else primaryColor.copy(alpha = 0.75f),
        topLeft = Offset(x + nestPadding, y + nestPadding),
        size = Size(nestSize, nestSize),
        cornerRadius = CornerRadius(18f, 18f),
        style = Stroke(width = if (isActive) 3.2f else 2f)
    )

    // 4 Recessed 3D Glossy Circular Pawn Pedestals
    val nestCenterX = x + baseSize / 2f
    val nestCenterY = y + baseSize / 2f
    val offsetDist = nestSize * 0.255f

    val nestCirclePositions = listOf(
        Offset(nestCenterX - offsetDist, nestCenterY - offsetDist),
        Offset(nestCenterX + offsetDist, nestCenterY - offsetDist),
        Offset(nestCenterX - offsetDist, nestCenterY + offsetDist),
        Offset(nestCenterX + offsetDist, nestCenterY + offsetDist)
    )

    for (pos in nestCirclePositions) {
        val spotR = cellSize * 0.49f
        // Outer metallic ring
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(lightColor, darkColor),
                start = Offset(pos.x - spotR, pos.y - spotR),
                end = Offset(pos.x + spotR, pos.y + spotR)
            ),
            radius = spotR,
            center = pos
        )
        // Glossy inner pedestal cup
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, lightColor.copy(alpha = 0.65f), primaryColor),
                center = Offset(pos.x - spotR * 0.22f, pos.y - spotR * 0.22f),
                radius = spotR * 1.15f
            ),
            radius = spotR * 0.80f,
            center = pos
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = spotR * 0.80f,
            center = pos,
            style = Stroke(width = 1.6f)
        )
    }
}

private fun DrawScope.drawGlossy3DTile(
    x: Float,
    y: Float,
    cellSize: Float,
    topColor: Color,
    bottomColor: Color,
    isColored: Boolean
) {
    val pad = 1.1f
    val tileSize = (cellSize - pad * 2f).coerceAtLeast(1f)
    val cr = CornerRadius(cellSize * 0.16f, cellSize * 0.16f)

    // Recessed tile groove background
    drawRect(
        color = TrackTileGroove.copy(alpha = 0.42f),
        topLeft = Offset(x, y),
        size = Size(cellSize, cellSize)
    )

    // Glossy rounded 3D raised tile face
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(topColor, bottomColor),
            startY = y + pad,
            endY = y + cellSize - pad
        ),
        topLeft = Offset(x + pad, y + pad),
        size = Size(tileSize, tileSize),
        cornerRadius = cr
    )

    // Subtle top specular highlight strip
    drawRoundRect(
        color = Color.White.copy(alpha = if (isColored) 0.34f else 0.72f),
        topLeft = Offset(x + pad + 1.2f, y + pad + 1f),
        size = Size((tileSize - 2.4f).coerceAtLeast(1f), (tileSize * 0.30f).coerceAtLeast(1f)),
        cornerRadius = CornerRadius(cellSize * 0.12f, cellSize * 0.12f)
    )

    // Crisp bevel outline
    drawRoundRect(
        color = if (isColored) Color.White.copy(alpha = 0.55f) else Color(0xFF94A3B8).copy(alpha = 0.55f),
        topLeft = Offset(x + pad, y + pad),
        size = Size(tileSize, tileSize),
        cornerRadius = cr,
        style = Stroke(width = 1.0f)
    )
}

private fun DrawScope.draw3DSafeStar(
    cx: Float,
    cy: Float,
    radius: Float,
    glowAlpha: Float,
    onColoredTile: Boolean
) {
    // Warm golden glow halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(StarGold.copy(alpha = 0.45f * glowAlpha), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius * 1.45f
        ),
        radius = radius * 1.45f,
        center = Offset(cx, cy)
    )

    val path = Path()
    val shadowPath = Path()
    val innerRadius = radius * 0.45f
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * Math.PI / 5) - (Math.PI / 2)
        val px = cx + (r * cos(angle)).toFloat()
        val py = cy + (r * sin(angle)).toFloat()
        if (i == 0) {
            path.moveTo(px, py)
            shadowPath.moveTo(px + 1.2f, py + 1.8f)
        } else {
            path.lineTo(px, py)
            shadowPath.lineTo(px + 1.2f, py + 1.8f)
        }
    }
    path.close()
    shadowPath.close()

    // Soft 3D drop shadow
    drawPath(shadowPath, color = Color(0xFF0F172A).copy(alpha = 0.30f), style = Fill)

    // Glossy 3D Star fill (White-Gold on colored start tiles, Rich Metallic Gold on Ivory tiles)
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = if (onColoredTile) listOf(Color.White, StarGoldBright, StarGold)
            else listOf(StarGoldBright, StarGold, StarGoldDeep)
        ),
        style = Fill
    )
    drawPath(
        path = path,
        color = if (onColoredTile) Color.White else StarGoldDeep,
        style = Stroke(width = 1.3f)
    )
}
