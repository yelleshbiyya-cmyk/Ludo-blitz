package com.example.ludoblitz.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.service.MatchmakingResult
import com.example.ludoblitz.ui.components.AnimatedArcadeBackground
import com.example.ludoblitz.ui.components.Game3DAvatarBadge
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.Game3DTrophyCanvas
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import kotlinx.coroutines.delay

@Composable
fun MatchmakingScreen(
    profile: PlayerProfile,
    mode: GameMode,
    onOpponentFoundSound: () -> Unit = {},
    onCountdownBeepSound: (Int) -> Unit = {},
    onMatchFound: () -> Unit,
    modifier: Modifier = Modifier
) {
    var matchInfo by remember { mutableStateOf<MatchmakingResult?>(null) }
    var countdownSec by remember { mutableIntStateOf(3) }
    val radarRotation = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "mmPulse")
    val vsScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vsScale"
    )

    LaunchedEffect(Unit) {
        radarRotation.animateTo(
            targetValue = 720f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    LaunchedEffect(Unit) {
        delay(1200)
        matchInfo = MatchmakingResult(
            matchId = "match_live_01",
            opponentName = listOf("CyberViper", "ShadowRunner", "ApexRoller", "NeonKnight").random(),
            opponentAvatar = "🤖",
            opponentTrophies = (profile.trophies + (-20..30).random()).coerceAtLeast(60),
            opponentLevel = profile.level.coerceAtLeast(1),
            isRealTime = false
        )
        onOpponentFoundSound()

        while (countdownSec > 0) {
            onCountdownBeepSound(countdownSec)
            delay(550)
            countdownSec--
        }
        delay(200)
        onMatchFound()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedArcadeBackground()

        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(26.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1B4B).copy(alpha = 0.65f))
                    .border(1.6.dp, if (matchInfo == null) BentoSky else RoyalGoldLight, RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (matchInfo == null) "⚡ SEARCHING FOR OPPONENT..." else "👑 OPPONENT FOUND!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = if (matchInfo == null) Color(0xFFBAE6FD) else RoyalGoldLight,
                    letterSpacing = 1.1.sp
                )
            }

            // 3D ROYAL VS MATCH ARENA CARD
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF4338CA), Color(0xFF312E81), Color(0xFF581C87)),
                borderColors = listOf(RoyalGoldLight, BentoSky, RoyalGoldLight),
                cornerRadius = 26.dp,
                borderWidth = 2.4.dp,
                shadowColor = BentoAmber
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Game3DAvatarBadge(
                            emoji = "🦸",
                            level = profile.level,
                            size = 84.dp,
                            ringColors = listOf(RoyalGoldLight, BentoSky),
                            bgColors = listOf(Color(0xFF38BDF8), Color(0xFF2563EB), Color(0xFF1E1B4B))
                        )
                        Text(profile.username, fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Game3DTrophyCanvas(size = 16.dp)
                            Text("${profile.trophies}", fontSize = 13.sp, color = RoyalGoldLight, fontWeight = FontWeight.Black)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .scale(vsScale)
                            .shadow(12.dp, CircleShape, ambientColor = BentoRose)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(RoyalGoldLight, BentoRose)))
                            .border(2.2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(modifier = Modifier.rotate(if (matchInfo == null) radarRotation.value else 0f)) {
                            Game3DAvatarBadge(
                                emoji = matchInfo?.opponentAvatar ?: "🔍",
                                level = matchInfo?.opponentLevel,
                                size = 84.dp,
                                ringColors = listOf(RoyalGoldLight, BentoRose),
                                bgColors = listOf(Color(0xFFFB7185), Color(0xFFBE123C), Color(0xFF4C0519))
                            )
                        }
                        Text(
                            matchInfo?.opponentName ?: "Searching...",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (matchInfo != null) {
                                Game3DTrophyCanvas(size = 16.dp)
                                Text("${matchInfo?.opponentTrophies}", fontSize = 13.sp, color = RoyalGoldLight, fontWeight = FontWeight.Black)
                            } else {
                                Text("Matching...", fontSize = 12.sp, color = Color(0xFFE0E7FF), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (matchInfo != null) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .scale(vsScale)
                        .shadow(18.dp, CircleShape, ambientColor = RoyalGoldLight)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(RoyalGoldLight, BentoAmber, RoyalGoldDark)))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (countdownSec > 0) "$countdownSec" else "GO!",
                        fontSize = if (countdownSec > 0) 32.sp else 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E1B4B)
                    )
                }
            } else {
                Text(
                    text = "Preparing 60s Royal Blitz Arena...",
                    fontSize = 13.sp,
                    color = Color(0xFFE0E7FF),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
