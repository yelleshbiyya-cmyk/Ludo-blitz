package com.example.ludoblitz.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ludoblitz.model.BattleResult
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.service.LocalizationManager
import com.example.ludoblitz.ui.components.AnimatedArcadeBackground
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.Game3DTrophyCanvas
import com.example.ludoblitz.ui.components.HdDuckCoinIcon
import com.example.ludoblitz.ui.components.HdGemIcon
import com.example.ludoblitz.ui.components.drawHdDuckCoin
import com.example.ludoblitz.ui.components.drawHdGem
import com.example.ludoblitz.viewmodel.BattleViewModel
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import kotlin.math.sin

@Composable
fun BattleResultScreen(
    battleViewModel: BattleViewModel,
    profile: PlayerProfile,
    onRematch: () -> Unit,
    onHome: (coinsEarned: Int, gemsEarned: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by battleViewModel.gameState.collectAsState()
    val isVictory = state.result == BattleResult.VICTORY
    val isDraw = state.result == BattleResult.DRAW
    val lang = profile.languageCode

    val headline = when {
        isVictory -> LocalizationManager.getString("victory_title", lang)
        isDraw -> LocalizationManager.getString("draw_title", lang)
        else -> LocalizationManager.getString("defeat_title", lang)
    }

    val trophyDelta = if (isVictory) "+35" else if (isDraw) "+5" else "-15"
    val coinsEarned = if (isVictory) 150 else 40
    val gemsEarned = if (isVictory) 1 else 0
    val xpEarned = if (isVictory) 80 else 25

    val animatedCoins by animateIntAsState(
        targetValue = coinsEarned,
        animationSpec = tween(800),
        label = "resultCoins"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "resultAnim")
    val confettiProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiY"
    )
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgePulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("battle_result_screen"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedArcadeBackground()

        // Celebratory Confetti + Fireworks + Floating HD Duck Coins & Gems Shower
        Canvas(modifier = Modifier.fillMaxSize()) {
            val confettiColors = listOf(RoyalGoldLight, BentoAmber, BentoEmerald, BentoSky, BentoRose, BentoPurple, Color.White)
            val particleCount = if (isVictory) 40 else 16

            if (isVictory) {
                val fwPhase1 = confettiProgress
                val fwPhase2 = (confettiProgress + 0.5f) % 1f
                val fwCenters = listOf(
                    Offset(size.width * 0.22f, size.height * 0.20f) to fwPhase1,
                    Offset(size.width * 0.78f, size.height * 0.22f) to fwPhase2
                )
                for ((center, phase) in fwCenters) {
                    val alpha = (1f - phase).coerceIn(0f, 1f) * 0.88f
                    val radius = 24f + phase * 115f
                    for (ray in 0 until 10) {
                        val angle = (ray / 10f) * 2f * kotlin.math.PI.toFloat()
                        val px = center.x + kotlin.math.cos(angle) * radius
                        val py = center.y + kotlin.math.sin(angle) * radius
                        drawCircle(
                            color = confettiColors[ray % confettiColors.size].copy(alpha = alpha),
                            radius = 5.5f * (1f - phase * 0.5f),
                            center = Offset(px, py)
                        )
                    }
                }
            }

            for (i in 0 until particleCount) {
                val startX = (size.width / particleCount) * i + 12f
                val speedMultiplier = (i % 4 + 2) * 0.25f
                val currentY = ((confettiProgress * size.height * speedMultiplier) + (i * 53)) % size.height
                val px = startX + sin(currentY * 0.03f + i) * 24f

                if (isVictory && i % 6 == 0) {
                    drawHdDuckCoin(center = Offset(px, currentY), radius = 14f, shimmerPhase = confettiProgress)
                } else if (isVictory && i % 9 == 0) {
                    drawHdGem(center = Offset(px, currentY), radius = 13f, sparkleAlpha = 0.9f)
                } else {
                    val pColor = confettiColors[i % confettiColors.size]
                    drawCircle(
                        color = pColor.copy(alpha = 0.85f),
                        radius = if (i % 3 == 0) 6f else 4f,
                        center = Offset(px, currentY)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HD 3D Victory / Defeat Medallion
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(badgePulse)
                    .shadow(
                        22.dp,
                        CircleShape,
                        ambientColor = if (isVictory) RoyalGoldLight else BentoRose,
                        spotColor = if (isVictory) RoyalGoldLight else BentoRose
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (isVictory) listOf(RoyalGoldLight, BentoAmber, RoyalGoldDark)
                            else listOf(Color(0xFFFB7185), Color(0xFFBE123C), Color(0xFF881337))
                        )
                    )
                    .border(3.2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isVictory) {
                    Image(
                        painter = painterResource(id = R.drawable.img_victory_crown_art),
                        contentDescription = "Victory Crown",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("💥", fontSize = 54.sp)
                }
            }

            Text(
                text = headline,
                fontSize = 33.sp,
                fontWeight = FontWeight.Black,
                color = if (isVictory) RoyalGoldLight else if (isDraw) BentoSky else Color(0xFFFDA4AF),
                letterSpacing = 1.2.sp
            )

            state.winReason?.let { reason ->
                Text(
                    text = reason,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Glossy Royal 3D Score & Standings Card
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF3730A3), Color(0xFF2E1065)),
                borderColors = listOf(RoyalGoldLight, BentoSky, RoyalGoldLight),
                cornerRadius = 24.dp,
                borderWidth = 2.2.dp,
                shadowColor = if (isVictory) BentoAmber else BentoPurple
            ) {
                if (state.isClassic) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "👑 FINAL STANDINGS",
                            fontWeight = FontWeight.Black,
                            color = RoyalGoldLight,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )

                        for (player in state.activePlayers) {
                            val isWinner = state.winnerSide == player
                            val (color, name) = when (player) {
                                PlayerSide.RED -> Color(0xFFF43F5E) to "RED"
                                PlayerSide.GREEN -> Color(0xFF10B981) to "GREEN"
                                PlayerSide.YELLOW -> Color(0xFFFBBF24) to "YELLOW"
                                PlayerSide.BLUE -> Color(0xFF38BDF8) to "BLUE"
                            }
                            val finished = state.allTokens[player]?.count { it.isFinished } ?: 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isWinner) color.copy(alpha = 0.35f) else Color(0xFF1E1B4B).copy(alpha = 0.65f))
                                    .border(1.4.dp, if (isWinner) RoyalGoldLight else color.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                    Text(
                                        text = if (isWinner) "👑 $name (WINNER)" else name,
                                        fontWeight = if (isWinner) FontWeight.Black else FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "🏁 $finished/4 Home",
                                    fontWeight = FontWeight.Black,
                                    color = if (finished == 4) Color(0xFF86EFAC) else RoyalGoldLight,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        ResultRewardsRow(
                            trophyDelta = null,
                            coinsEarned = animatedCoins,
                            gemsEarned = gemsEarned,
                            xpEarned = xpEarned
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("YOU", fontWeight = FontWeight.Black, color = Color(0xFFBAE6FD), fontSize = 12.sp, letterSpacing = 1.sp)
                                Text("${state.totalPlayerScore}", fontWeight = FontWeight.Black, fontSize = 34.sp, color = Color.White)
                                Text("👑 ${state.playerTokens.count { it.isFinished }}/4 Finished", fontSize = 11.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Brush.radialGradient(listOf(RoyalGoldLight, BentoAmber)))
                                    .border(1.5.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("VS", fontWeight = FontWeight.Black, color = Color(0xFF1E1B4B), fontSize = 15.sp)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RIVAL", fontWeight = FontWeight.Black, color = Color(0xFFFDA4AF), fontSize = 12.sp, letterSpacing = 1.sp)
                                Text("${state.totalOpponentScore}", fontWeight = FontWeight.Black, fontSize = 34.sp, color = Color.White)
                                Text("👑 ${state.opponentTokens.count { it.isFinished }}/4 Finished", fontSize = 11.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Bold)
                            }
                        }

                        ResultRewardsRow(
                            trophyDelta = trophyDelta,
                            coinsEarned = animatedCoins,
                            gemsEarned = gemsEarned,
                            xpEarned = xpEarned
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Tactile 3D Action Buttons (Home with Flying Coins/Gems & Rematch)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Game3DButton(
                    onClick = { onHome(coinsEarned, gemsEarned) },
                    topColor = RoyalGoldLight,
                    bottomColor = BentoAmber,
                    ledgeColor = RoyalGoldDark,
                    height = 56.dp,
                    cornerRadius = 18.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_from_result_btn")
                ) {
                    Text(
                        LocalizationManager.getString("home_btn", lang).uppercase(),
                        color = Color(0xFF1E1B4B),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Game3DButton(
                    onClick = onRematch,
                    topColor = Color(0xFF4ADE80),
                    bottomColor = Color(0xFF16A34A),
                    ledgeColor = Color(0xFF14532D),
                    height = 56.dp,
                    cornerRadius = 18.dp,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rematch_btn")
                ) {
                    Text(
                        LocalizationManager.getString("rematch_btn", lang).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultRewardsRow(
    trophyDelta: String?,
    coinsEarned: Int,
    gemsEarned: Int,
    xpEarned: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1B4B).copy(alpha = 0.72f))
            .border(1.4.dp, RoyalGoldLight.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (trophyDelta != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Game3DTrophyCanvas(size = 19.dp)
                Text(trophyDelta, fontWeight = FontWeight.Black, color = RoyalGoldLight, fontSize = 14.sp)
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            HdDuckCoinIcon(size = 22.dp, animated = true)
            Text("+$coinsEarned", fontWeight = FontWeight.Black, color = RoyalGoldLight, fontSize = 15.sp)
        }
        if (gemsEarned > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HdGemIcon(size = 20.dp, animated = true)
                Text("+$gemsEarned", fontWeight = FontWeight.Black, color = BentoSky, fontSize = 15.sp)
            }
        }
        Text("⚡ +$xpEarned XP", fontWeight = FontWeight.Black, color = Color(0xFF86EFAC), fontSize = 14.sp)
    }
}
