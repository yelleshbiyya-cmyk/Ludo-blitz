package com.example.ludoblitz.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludoblitz.model.GameState
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.model.Token
import com.example.ludoblitz.service.LocalizationManager
import com.example.ludoblitz.ui.components.ActiveEmoteBubble
import com.example.ludoblitz.ui.components.CountdownOverlay
import com.example.ludoblitz.ui.components.DiceView
import com.example.ludoblitz.ui.components.FloatingEmotePopup
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.LudoBoardView
import com.example.ludoblitz.ui.components.PowerUpBar
import com.example.ludoblitz.ui.components.Premium3DTokenCanvas
import com.example.ludoblitz.ui.components.QuickEmoteTray
import com.example.ludoblitz.ui.components.TimerBar
import com.example.ludoblitz.viewmodel.BattleViewModel
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldLight

@Composable
fun BattleScreen(
    battleViewModel: BattleViewModel,
    profile: PlayerProfile,
    onMatchCompleted: () -> Unit,
    onExitToHome: () -> Unit = {},
    onToggleAudioQuick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by battleViewModel.gameState.collectAsState()
    val lang = profile.languageCode

    var emoteTrayExpanded by remember { mutableStateOf(false) }
    var activeEmoteBubble by remember { mutableStateOf<ActiveEmoteBubble?>(null) }
    var lastObservedCaptures by remember { mutableIntStateOf(state.stats.playerCaptures) }
    var lastObservedHomeCount by remember { mutableIntStateOf(state.playerTokens.count { it.isFinished }) }

    BackHandler {
        onExitToHome()
    }

    LaunchedEffect(state.isGameOver) {
        if (state.isGameOver) {
            onMatchCompleted()
        }
    }

    // Playful automatic AI opponent emote reactions during exciting match moments
    LaunchedEffect(state.stats.playerCaptures) {
        if (state.stats.playerCaptures > lastObservedCaptures && !state.isPassAndPlay) {
            activeEmoteBubble = ActiveEmoteBubble(
                id = System.nanoTime(),
                emoji = "😱",
                label = "Whoa! Nice Capture!",
                senderName = "Rival",
                isFromOpponent = true
            )
        }
        lastObservedCaptures = state.stats.playerCaptures
    }

    LaunchedEffect(state.playerTokens.count { it.isFinished }) {
        val finished = state.playerTokens.count { it.isFinished }
        if (finished > lastObservedHomeCount && !state.isPassAndPlay) {
            activeEmoteBubble = ActiveEmoteBubble(
                id = System.nanoTime(),
                emoji = "👏",
                label = "Awesome Finish!",
                senderName = "Rival",
                isFromOpponent = true
            )
        }
        lastObservedHomeCount = finished
    }

    val isAudioOn = profile.soundEnabled || profile.musicEnabled

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("battle_screen_root")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. TOP ROYAL 3D ARENA HEADER + HOME EXIT & QUICK AUDIO TOGGLE
            if (state.isClassic) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Game3DButton(
                            onClick = onExitToHome,
                            topColor = Color(0xFF4F46E5),
                            bottomColor = Color(0xFF312E81),
                            ledgeColor = Color(0xFF1E1B4B),
                            borderColor = RoyalGoldLight,
                            cornerRadius = 12.dp,
                            height = 38.dp,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("battle_home_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Back to Home",
                                tint = RoyalGoldLight,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Game3DButton(
                            onClick = onToggleAudioQuick,
                            topColor = if (isAudioOn) Color(0xFF10B981) else Color(0xFFF43F5E),
                            bottomColor = if (isAudioOn) Color(0xFF047857) else Color(0xFFBE123C),
                            ledgeColor = if (isAudioOn) Color(0xFF064E3B) else Color(0xFF881337),
                            borderColor = Color.White.copy(alpha = 0.85f),
                            cornerRadius = 12.dp,
                            height = 38.dp,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("battle_audio_toggle_button")
                        ) {
                            Text(
                                text = if (isAudioOn) "🔊" else "🔇",
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E1B4B).copy(alpha = 0.62f))
                                .border(1.2.dp, RoyalGoldLight.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (state.isPassAndPlay) "LOCAL PASS & PLAY" else "CLASSIC LUDO",
                                color = RoyalGoldLight,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${state.activePlayers.size} PLAYERS",
                                color = Color(0xFFBAE6FD),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Full-width Classic player pills strip so 4 players never crowd or clip
                    ClassicBattleHeader(state = state)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Game3DButton(
                        onClick = onExitToHome,
                        topColor = Color(0xFF4F46E5),
                        bottomColor = Color(0xFF312E81),
                        ledgeColor = Color(0xFF1E1B4B),
                        borderColor = RoyalGoldLight,
                        cornerRadius = 12.dp,
                        height = 38.dp,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("battle_home_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back to Home",
                            tint = RoyalGoldLight,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Game3DButton(
                        onClick = onToggleAudioQuick,
                        topColor = if (isAudioOn) Color(0xFF10B981) else Color(0xFFF43F5E),
                        bottomColor = if (isAudioOn) Color(0xFF047857) else Color(0xFFBE123C),
                        ledgeColor = if (isAudioOn) Color(0xFF064E3B) else Color(0xFF881337),
                        borderColor = Color.White.copy(alpha = 0.85f),
                        cornerRadius = 12.dp,
                        height = 38.dp,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("battle_audio_toggle_button")
                    ) {
                        Text(
                            text = if (isAudioOn) "🔊" else "🔇",
                            fontSize = 15.sp
                        )
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        BentoBattleHeader(
                            profile = profile,
                            state = state
                        )
                    }
                }
            }

            // 2. VIBRANT GLOSSY TURN STATUS BANNER
            DynamicStatusBanner(
                message = state.activeBannerMessage,
                currentTurn = state.currentTurn,
                hasRolled = state.hasRolled,
                selectableCount = state.selectableTokenIds.size,
                isClassic = state.isClassic,
                isCurrentTurnHuman = state.isCurrentTurnHuman(),
                lang = lang
            )

            // 3. BIG FULL-WIDTH 15x15 ROYAL 3D LUDO BOARD
            LudoBoardView(
                playerTokens = state.playerTokens,
                opponentTokens = state.opponentTokens,
                allTokensMap = state.allTokens,
                selectableTokenIds = state.selectableTokenIds,
                currentTurn = state.currentTurn,
                diceValue = state.diceValue,
                hasRolled = state.hasRolled,
                isClassic = state.isClassic,
                onTokenClicked = { tokenId ->
                    battleViewModel.onPlayerSelectToken(tokenId)
                },
                onTokenStepSound = { stepIdx, totalSteps ->
                    battleViewModel.soundManager.playPawnStep(stepIdx, totalSteps)
                },
                onTokenLandSound = {
                    battleViewModel.soundManager.playPawnLand()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp)
            )

            // 4. 3D POWER-UP TRAY (Blitz Only)
            if (!state.isClassic) {
                PowerUpBar(
                    chargesMap = state.playerPowerUpCharges,
                    onPowerUpUsed = { powerUp ->
                        battleViewModel.onPlayerUsePowerUp(powerUp)
                    }
                )
            }

            // 5. BOTTOM CONTROLS: Glowing Active-Player Dock & Big 3D Dice
            PlayerControlRow(
                state = state,
                lang = lang,
                onRollClicked = {
                    battleViewModel.onPlayerRollDice()
                }
            )

            // 6. BOTTOM FOOTER CHIP + QUICK EMOTE REACTIONS BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (!state.isClassic) {
                        BentoBottomMissionChip(
                            playerScore = state.totalPlayerScore,
                            captures = state.stats.playerCaptures
                        )
                    } else {
                        ClassicRulesFooterChip()
                    }
                }

                QuickEmoteTray(
                    expanded = emoteTrayExpanded,
                    onToggleExpanded = {
                        battleViewModel.soundManager.playButtonClick()
                        emoteTrayExpanded = !emoteTrayExpanded
                    },
                    onSelectEmote = { emote ->
                        battleViewModel.soundManager.playTurnNotification()
                        emoteTrayExpanded = false
                        activeEmoteBubble = ActiveEmoteBubble(
                            id = System.nanoTime(),
                            emoji = emote.emoji,
                            label = emote.label,
                            senderName = profile.username.take(10),
                            isFromOpponent = false
                        )
                    }
                )
            }
        }

        // Animated Floating Emote Reaction Popup above the Board
        FloatingEmotePopup(
            bubble = activeEmoteBubble,
            onFinished = { activeEmoteBubble = null },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 86.dp)
        )

        // Fullscreen 5..4..3..2..1 Countdown Overlay
        CountdownOverlay(number = state.countdownOverlayNumber)
    }
}

@Composable
private fun BentoBattleHeader(
    profile: PlayerProfile,
    state: GameState
) {
    val isPlayerTurn = state.currentTurn == PlayerSide.PLAYER
    val isOpponentTurn = state.currentTurn == PlayerSide.OPPONENT

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Player 1 Glossy Pill (Red)
        Row(
            modifier = Modifier
                .shadow(if (isPlayerTurn) 10.dp else 4.dp, RoundedCornerShape(24.dp), ambientColor = Color(0xFFF43F5E))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isPlayerTurn) listOf(Color(0xFFBE123C), Color(0xFF881337))
                        else listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                    )
                )
                .border(
                    width = if (isPlayerTurn) 2.2.dp else 1.2.dp,
                    color = if (isPlayerTurn) RoyalGoldLight else Color(0xFF60A5FA).copy(alpha = 0.65f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 4.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFB7185), Color(0xFFBE123C))))
                    .border(1.5.dp, if (isPlayerTurn) RoyalGoldLight else Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isPlayerTurn) "▶" else "LV${profile.level}", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Column {
                Text(
                    text = profile.username.take(8),
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.totalPlayerScore} pts",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = RoyalGoldLight
                )
            }
        }

        // Center 60s Match Countdown
        TimerBar(
            matchTimeRemaining = state.matchTimeRemainingSeconds,
            turnTimeRemaining = state.turnTimeRemainingSeconds,
            isFinalTenSeconds = state.isFinalTenSeconds,
            countdownOverlayNumber = state.countdownOverlayNumber,
            modifier = Modifier.weight(1f)
        )

        // Rival Glossy Pill (Yellow)
        Row(
            modifier = Modifier
                .shadow(if (isOpponentTurn) 10.dp else 4.dp, RoundedCornerShape(24.dp), ambientColor = BentoAmber)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isOpponentTurn) listOf(Color(0xFFB45309), Color(0xFF78350F))
                        else listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                    )
                )
                .border(
                    width = if (isOpponentTurn) 2.2.dp else 1.2.dp,
                    color = if (isOpponentTurn) RoyalGoldLight else Color(0xFF60A5FA).copy(alpha = 0.65f),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Rival_Bot",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.totalOpponentScore} pts",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isOpponentTurn) RoyalGoldLight else Color(0xFFBAE6FD)
                )
            }
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(RoyalGoldLight, BentoAmber)))
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isOpponentTurn) "▶" else "BOT", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E1B4B))
            }
        }
    }
}

@Composable
private fun ClassicBattleHeader(
    state: GameState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (player in state.activePlayers) {
            val isTurn = state.currentTurn == player
            val (color, darkColor, name) = when (player) {
                PlayerSide.RED -> Triple(Color(0xFFF43F5E), Color(0xFF9F1239), "RED")
                PlayerSide.GREEN -> Triple(Color(0xFF10B981), Color(0xFF047857), "GRN")
                PlayerSide.YELLOW -> Triple(Color(0xFFFBBF24), Color(0xFFB45309), "YLW")
                PlayerSide.BLUE -> Triple(Color(0xFF38BDF8), Color(0xFF1D4ED8), "BLU")
            }

            val finishedCount = state.allTokens[player]?.count { it.isFinished } ?: 0

            Row(
                modifier = Modifier
                    .weight(1f)
                    .shadow(if (isTurn) 8.dp else 2.dp, RoundedCornerShape(12.dp), ambientColor = color)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            if (isTurn) listOf(color.copy(alpha = 0.92f), darkColor)
                            else listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                        )
                    )
                    .border(
                        width = if (isTurn) 2.2.dp else 1.dp,
                        color = if (isTurn) Color.White else color.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (isTurn) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (isTurn) Color.White else color)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    Text(
                        text = if (isTurn) "▶$name" else name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Text(
                    text = "👑$finishedCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = if (finishedCount > 0) RoyalGoldLight else Color(0xFFE0E7FF),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
private fun DynamicStatusBanner(
    message: String?,
    currentTurn: PlayerSide,
    hasRolled: Boolean,
    selectableCount: Int,
    isClassic: Boolean = false,
    isCurrentTurnHuman: Boolean = true,
    lang: String
) {
    val turnName = when (currentTurn) {
        PlayerSide.RED -> "RED"
        PlayerSide.GREEN -> "GREEN"
        PlayerSide.YELLOW -> "YELLOW"
        PlayerSide.BLUE -> "BLUE"
    }

    val turnColor = when (currentTurn) {
        PlayerSide.RED -> Color(0xFFF43F5E)
        PlayerSide.GREEN -> Color(0xFF10B981)
        PlayerSide.YELLOW -> Color(0xFFFBBF24)
        PlayerSide.BLUE -> Color(0xFF38BDF8)
    }

    val text = when {
        message != null -> message
        isClassic && !isCurrentTurnHuman -> "$turnName (AI) is rolling..."
        isClassic && isCurrentTurnHuman && !hasRolled -> "$turnName's Turn — Tap Dice to Roll!"
        isClassic && isCurrentTurnHuman && hasRolled && selectableCount > 0 -> "$turnName — Tap a glowing pawn!"
        isClassic && isCurrentTurnHuman && hasRolled && selectableCount == 0 -> "$turnName — No moves available"
        currentTurn == PlayerSide.PLAYER && !hasRolled -> LocalizationManager.getString("tap_to_roll", lang)
        currentTurn == PlayerSide.PLAYER && hasRolled && selectableCount > 0 -> LocalizationManager.getString("select_token_prompt", lang)
        currentTurn == PlayerSide.OPPONENT -> LocalizationManager.getString("opponent_turn", lang)
        else -> "Battle Active!"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .shadow(8.dp, RoundedCornerShape(14.dp), ambientColor = turnColor)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2E3A87),
                        turnColor.copy(alpha = 0.55f),
                        Color(0xFF2E3A87)
                    )
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
            .padding(vertical = 6.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 0.4.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PlayerControlRow(
    state: GameState,
    lang: String,
    onRollClicked: () -> Unit
) {
    val isHumanTurn = state.isCurrentTurnHuman()
    val canRoll = isHumanTurn && !state.hasRolled && !state.isGameOver && !state.isRolling

    val turnName = when (state.currentTurn) {
        PlayerSide.RED -> "RED"
        PlayerSide.GREEN -> "GREEN"
        PlayerSide.YELLOW -> "YELLOW"
        PlayerSide.BLUE -> "BLUE"
    }

    val (rawTurnColor, rawDarkColor) = when (state.currentTurn) {
        PlayerSide.RED -> Color(0xFFF43F5E) to Color(0xFF9F1239)
        PlayerSide.GREEN -> Color(0xFF10B981) to Color(0xFF047857)
        PlayerSide.YELLOW -> Color(0xFFFBBF24) to Color(0xFFB45309)
        PlayerSide.BLUE -> Color(0xFF38BDF8) to Color(0xFF1D4ED8)
    }

    val turnColor by animateColorAsState(rawTurnColor, tween(280), label = "turnCardMain")
    val darkColor by animateColorAsState(rawDarkColor, tween(280), label = "turnCardDark")

    val roleLabel = when {
        state.isPassAndPlay -> "$turnName'S TURN"
        isHumanTurn && state.isClassic -> "YOU ($turnName)"
        !isHumanTurn -> "$turnName (BOT)"
        else -> "YOUR TURN ($turnName)"
    }

    val actionSubtitle = when {
        state.isRolling -> "Rolling 3D dice..."
        canRoll -> "Tap here or 3D Dice to Roll!"
        isHumanTurn && state.selectableTokenIds.isNotEmpty() -> "Pick a glowing pawn (targets shown!)"
        !isHumanTurn -> "$turnName is making a move..."
        else -> "Preparing next turn..."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .shadow(16.dp, RoundedCornerShape(22.dp), ambientColor = turnColor, spotColor = turnColor)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        darkColor,
                        Color(0xFF312E81),
                        darkColor.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = if (canRoll) 2.8.dp else 2.dp,
                brush = Brush.linearGradient(
                    if (canRoll) listOf(RoyalGoldLight, Color.White, RoyalGoldLight)
                    else listOf(turnColor, Color.White.copy(alpha = 0.6f))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(enabled = canRoll) { onRollClicked() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(8.dp, CircleShape, ambientColor = turnColor)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(turnColor.copy(alpha = 0.7f), Color(0xFF1E1B4B))))
                    .border(2.2.dp, RoyalGoldLight, CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Premium3DTokenCanvas(
                    token = Token(id = 0, side = state.currentTurn),
                    isSelectable = isHumanTurn && !state.isGameOver,
                    isMoving = state.isRolling
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(turnColor)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    Text(
                        text = roleLabel,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = actionSubtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canRoll) RoyalGoldLight else Color(0xFFF1F5F9)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.isClassic) {
                        val finished = state.allTokens[state.currentTurn]?.count { it.isFinished } ?: 0
                        Text("👑 $finished/4 Home", fontSize = 11.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                        if (state.consecutiveSixes > 0) {
                            Text("🔥 6s: ${state.consecutiveSixes}/3", fontSize = 11.sp, color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("⭐ ${state.totalPlayerScore} pts", fontSize = 11.sp, color = RoyalGoldLight, fontWeight = FontWeight.Bold)
                        Text("👑 ${state.playerTokens.count { it.isFinished }}/4", fontSize = 11.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        DiceView(
            diceValue = state.diceValue,
            secondDiceValue = state.secondDiceValue,
            isDoubleDice = state.isDoubleDiceActive,
            isRolling = state.isRolling,
            canRoll = canRoll,
            currentTurn = state.currentTurn,
            onRollClicked = onRollClicked
        )
    }
}

@Composable
private fun ClassicRulesFooterChip() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF312E81), Color(0xFF1E3A8A))))
            .border(1.2.dp, RoyalGoldLight.copy(alpha = 0.65f), RoundedCornerShape(13.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("👑", fontSize = 13.sp)
            Text(
                text = "Get all 4 pawns to Center Crown to Win!",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = "CLASSIC",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = RoyalGoldLight,
            letterSpacing = 1.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun BentoBottomMissionChip(
    playerScore: Int,
    captures: Int
) {
    val progress = (playerScore.toFloat() / 250f).coerceIn(0f, 1f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(Color(0xFF312E81), Color(0xFF1E3A8A))))
            .border(1.2.dp, BentoSky.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0284C7))
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🎯", fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(BentoRose)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("💥", fontSize = 11.sp)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF1E1B4B))
                .border(0.8.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(RoyalGoldLight, BentoAmber, BentoEmerald)))
            )
        }

        Text(
            text = "BLITZ GOAL",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = RoyalGoldLight,
            letterSpacing = 0.8.sp
        )
    }
}
