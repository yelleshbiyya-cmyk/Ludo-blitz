package com.example.ludoblitz.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ludoblitz.model.ChestType
import com.example.ludoblitz.model.ClassicMatchConfig
import com.example.ludoblitz.model.ClassicPlayerCount
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.model.GameState
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.service.LocalizationManager
import com.example.ludoblitz.ui.components.ChestRewardDialog
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.Game3DChestCanvas
import com.example.ludoblitz.ui.components.Game3DTrophyCanvas
import com.example.ludoblitz.ui.components.HdDuckCoinIcon
import com.example.ludoblitz.ui.components.HdGemIcon
import com.example.ludoblitz.ui.components.LuckyWheelDialog
import com.example.ludoblitz.ui.components.Section3DHeader
import com.example.ludoblitz.ui.components.TutorialDialog
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

@Composable
fun HomeScreen(
    profile: PlayerProfile,
    savedGameState: GameState? = null,
    freeSpinsLeft: Int = 3,
    onResumeGame: (GameState) -> Unit = {},
    onStartMatch: (GameMode) -> Unit,
    onStartClassicMatch: (ClassicMatchConfig) -> Unit = {},
    onOpenChest: (Int, Int) -> Unit = { _, _ -> },
    onWheelTick: () -> Unit = {},
    onLuckyWheelPrizeWon: (coins: Int, gems: Int, spinCostCoins: Int, prizeName: String) -> Unit = { _, _, _, _ -> },
    onOpenQuests: () -> Unit,
    onCompleteTutorial: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = profile.languageCode
    val infiniteTransition = rememberInfiniteTransition(label = "homeAnimations")
    val heroScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.022f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroScale"
    )
    val floatY by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    val sheenProgress by infiniteTransition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sheen"
    )

    var showChestDialog by remember { mutableStateOf(false) }
    var showLuckyWheelDialog by remember { mutableStateOf(false) }
    var openedChestType by remember { mutableStateOf(ChestType.WOODEN) }
    var showTutorial by remember { mutableStateOf(!profile.isTutorialCompleted) }

    if (showTutorial) {
        TutorialDialog(
            onDismiss = { showTutorial = false },
            onComplete = {
                showTutorial = false
                onCompleteTutorial()
            }
        )
    }

    if (showLuckyWheelDialog) {
        LuckyWheelDialog(
            freeSpinsLeft = freeSpinsLeft,
            currentCoins = profile.coins,
            onWheelTick = onWheelTick,
            onPrizeWon = onLuckyWheelPrizeWon,
            onDismiss = { showLuckyWheelDialog = false }
        )
    }

    if (showChestDialog) {
        val (coinsWon, gemsWon, xpWon) = when (openedChestType) {
            ChestType.WOODEN -> Triple(90, 2, 35)
            ChestType.SILVER -> Triple(140, 4, 55)
            ChestType.GOLD -> Triple(220, 7, 80)
            ChestType.BLITZ -> Triple(300, 10, 120)
        }
        ChestRewardDialog(
            chestType = openedChestType,
            coinsWon = coinsWon,
            gemsWon = gemsWon,
            xpWon = xpWon,
            onDismiss = {
                showChestDialog = false
                onOpenChest(coinsWon, gemsWon)
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ROYAL 3D GAME BRANDING HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .offset(y = (floatY * 0.5f).dp)
                        .shadow(16.dp, CircleShape, ambientColor = RoyalGold, spotColor = RoyalGold)
                        .clip(CircleShape)
                        .border(
                            2.8.dp,
                            Brush.linearGradient(listOf(RoyalGoldLight, BentoAmber, RoyalGoldLight)),
                            CircleShape
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_ludo_app_logo),
                        contentDescription = "Ludo Blitz Royal App Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column(horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString("app_title", lang).uppercase(),
                            fontSize = 27.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.4.sp
                        )
                        HdDuckCoinIcon(
                            size = 24.dp,
                            animated = true,
                            modifier = Modifier.offset(y = (floatY * 0.6f).dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = LocalizationManager.getString("app_tagline", lang),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = RoyalGoldLight,
                            letterSpacing = 1.8.sp
                        )
                        if (profile.winStreak > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C))))
                                    .border(1.dp, RoyalGoldLight, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🔥 ${profile.winStreak} STREAK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. RESUME SAVED GAME BANNER (If available)
        if (savedGameState != null && !savedGameState.isGameOver) {
            item {
                Game3DCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResumeGame(savedGameState) }
                        .testTag("resume_game_card"),
                    gradientColors = listOf(Color(0xFF78350F), Color(0xFF451A03)),
                    borderColors = listOf(RoyalGoldLight, BentoAmber),
                    shadowColor = BentoAmber
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .shadow(8.dp, CircleShape, ambientColor = RoyalGold)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(listOf(RoyalGoldLight, BentoAmber)))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Resume",
                                tint = Color(0xFF1E1B4B),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RESUME SAVED GAME",
                                fontWeight = FontWeight.Black,
                                color = RoyalGoldLight,
                                fontSize = 13.5.sp,
                                letterSpacing = 0.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${if (savedGameState.isClassic) "Classic Ludo" else "Blitz"} • ${savedGameState.activePlayers.size}P • Turn: ${savedGameState.currentTurn}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Game3DButton(
                            onClick = { onResumeGame(savedGameState) },
                            topColor = RoyalGoldLight,
                            bottomColor = BentoAmber,
                            ledgeColor = RoyalGoldDark,
                            height = 40.dp,
                            cornerRadius = 14.dp,
                            contentPadding = PaddingValues(horizontal = 13.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("resume_game_button")
                        ) {
                            Text(
                                text = "RESUME",
                                color = Color(0xFF1E1B4B),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }

        // 3. CINEMATIC 3D TURBO ARENA HERO BANNER (Instant 60s Blitz Match)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(194.dp)
                    .scale(heroScale)
                    .shadow(22.dp, RoundedCornerShape(26.dp), ambientColor = RoyalGold, spotColor = BentoIndigo)
                    .clip(RoundedCornerShape(26.dp))
                    .border(
                        width = 2.8.dp,
                        brush = Brush.linearGradient(listOf(RoyalGoldLight, BentoAmber, Color(0xFF60A5FA), RoyalGoldLight)),
                        shape = RoundedCornerShape(26.dp)
                    )
                    .clickable { onStartMatch(GameMode.QUICK_BATTLE) }
                    .testTag("hero_play_button")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ludo_hero_arena),
                    contentDescription = "Ludo Blitz 60s Arena Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Bright Royal Scrim for vivid legibility
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF1E1B4B).copy(alpha = 0.86f),
                                    Color(0xFF4C1D95).copy(alpha = 0.46f),
                                    Color(0xFF1E3A8A).copy(alpha = 0.25f)
                                )
                            )
                        )
                )

                // Animated Light Sheen Sweep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val bandX = size.width * sheenProgress
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.24f),
                                Color.Transparent
                            ),
                            start = Offset(bandX - 130f, 0f),
                            end = Offset(bandX + 130f, size.height)
                        )
                    )
                }

                // Floating Reward Chips (Top Right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .offset(y = (floatY * 0.8f).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E1B4B).copy(alpha = 0.72f))
                            .border(1.4.dp, RoyalGoldLight, RoundedCornerShape(14.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HdDuckCoinIcon(size = 18.dp, animated = true)
                        Text("+150", color = RoyalGoldLight, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0C4A6E).copy(alpha = 0.75f))
                            .border(1.4.dp, BentoSky, RoundedCornerShape(14.dp))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HdGemIcon(size = 18.dp, animated = true)
                        Text("WIN!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                }

                // Hero Headline + Tactile 3D PLAY BLITZ Push-Button
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Brush.horizontalGradient(listOf(BentoRose, BentoAmber)))
                            .border(1.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "⚡ 60-SECOND TURBO ARENA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PLAY BLITZ",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Fast 1v1 Power-Up Battle!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalGoldLight
                            )
                        }

                        Game3DButton(
                            onClick = { onStartMatch(GameMode.QUICK_BATTLE) },
                            topColor = RoyalGoldLight,
                            bottomColor = BentoAmber,
                            ledgeColor = RoyalGoldDark,
                            height = 48.dp,
                            cornerRadius = 16.dp,
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            Text("PLAY ▶", color = Color(0xFF1E1B4B), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // 4. PREMIUM LUCKY LUDO WHEEL BANNER
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .shadow(16.dp, RoundedCornerShape(22.dp), ambientColor = BentoPurple, spotColor = RoyalGold)
                    .clip(RoundedCornerShape(22.dp))
                    .border(
                        width = 2.2.dp,
                        brush = Brush.linearGradient(listOf(RoyalGoldLight, Color(0xFFC084FC), RoyalGoldLight)),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable { showLuckyWheelDialog = true }
                    .testTag("lucky_wheel_banner_card")
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_ludo_lucky_wheel_art),
                    contentDescription = "Lucky Ludo Fortune Spin Wheel",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF3B0764).copy(alpha = 0.90f),
                                    Color(0xFF581C87).copy(alpha = 0.65f),
                                    Color(0xFF1E3A8A).copy(alpha = 0.30f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (freeSpinsLeft > 0) listOf(Color(0xFF34D399), Color(0xFF059669))
                                        else listOf(RoyalGoldLight, BentoAmber)
                                    )
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.75f), RoundedCornerShape(9.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (freeSpinsLeft > 0) "🎁 $freeSpinsLeft FREE SPINS" else "🌟 JACKPOT 750+",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (freeSpinsLeft > 0) Color.White else Color(0xFF1E1B4B)
                            )
                        }
                        Text(
                            text = "LUCKY LUDO WHEEL",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "Spin for Duck Coins, Gems & Jackpots!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalGoldLight
                        )
                    }

                    Game3DButton(
                        onClick = { showLuckyWheelDialog = true },
                        topColor = Color(0xFF4ADE80),
                        bottomColor = Color(0xFF16A34A),
                        ledgeColor = Color(0xFF14532D),
                        height = 44.dp,
                        cornerRadius = 15.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.offset(y = (floatY * 0.45f).dp)
                    ) {
                        Text("🎡 SPIN!", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                    }
                }
            }
        }

        // 5. CLASSIC 15x15 LUDO MODES SECTION
        item {
            Section3DHeader(
                title = "🎲 CLASSIC 15×15 LUDO",
                badge = "FULL BOARD • 4 PAWNS",
                accentColor = RoyalGoldLight
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LargeModeCard(
                    title = "Classic 2-Player",
                    desc = "Red vs Yellow AI • 4 pawns to crown",
                    tag = "👑 VS AI (2P)",
                    tagColor = RoyalGoldLight,
                    iconEmoji = "🎯",
                    gradientColors = listOf(Color(0xFFBE123C), Color(0xFF881337), Color(0xFF78350F)),
                    badgeColors = listOf(Color(0xFFFB7185), Color(0xFFF59E0B)),
                    floatOffset = floatY,
                    modifier = Modifier.weight(1f)
                ) {
                    onStartClassicMatch(
                        ClassicMatchConfig(
                            playerCount = ClassicPlayerCount.TWO_PLAYERS,
                            isPassAndPlay = false
                        )
                    )
                }

                LargeModeCard(
                    title = "Classic 4-Player",
                    desc = "Red, Green, Yellow, Blue full battle",
                    tag = "🏆 VS AI (4P)",
                    tagColor = Color(0xFF86EFAC),
                    iconEmoji = "🌟",
                    gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46), Color(0xFF1D4ED8)),
                    badgeColors = listOf(Color(0xFF34D399), Color(0xFF38BDF8)),
                    floatOffset = -floatY,
                    modifier = Modifier.weight(1f)
                ) {
                    onStartClassicMatch(
                        ClassicMatchConfig(
                            playerCount = ClassicPlayerCount.FOUR_PLAYERS,
                            isPassAndPlay = false
                        )
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LargeModeCard(
                    title = "Pass & Play (2P)",
                    desc = "Local friends on 1 phone (Red & Yellow)",
                    tag = "📱 LOCAL (2P)",
                    tagColor = Color(0xFFBAE6FD),
                    iconEmoji = "🤝",
                    gradientColors = listOf(Color(0xFF0284C7), Color(0xFF1D4ED8), Color(0xFF4338CA)),
                    badgeColors = listOf(Color(0xFF38BDF8), Color(0xFF818CF8)),
                    floatOffset = -floatY,
                    modifier = Modifier.weight(1f)
                ) {
                    onStartClassicMatch(
                        ClassicMatchConfig(
                            playerCount = ClassicPlayerCount.TWO_PLAYERS,
                            isPassAndPlay = true
                        )
                    )
                }

                LargeModeCard(
                    title = "Pass & Play (4P)",
                    desc = "Family pass on 1 phone (4 players)",
                    tag = "🎉 LOCAL (4P)",
                    tagColor = Color(0xFFF5D0FE),
                    iconEmoji = "🎉",
                    gradientColors = listOf(Color(0xFF9333EA), Color(0xFF7E22CE), Color(0xFFBE185D)),
                    badgeColors = listOf(Color(0xFFC084FC), Color(0xFFF472B6)),
                    floatOffset = floatY,
                    modifier = Modifier.weight(1f)
                ) {
                    onStartClassicMatch(
                        ClassicMatchConfig(
                            playerCount = ClassicPlayerCount.FOUR_PLAYERS,
                            isPassAndPlay = true
                        )
                    )
                }
            }
        }

        // 6. REALISTIC 3D REWARD CHESTS ROW (Wooden, Silver, Gold, Blitz)
        item {
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF3730A3), Color(0xFF1E1B4B)),
                borderColors = listOf(RoyalGoldLight, BentoSky),
                shadowColor = BentoPurple
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            HdDuckCoinIcon(size = 20.dp, animated = true)
                            Text(
                                "3D REWARD CHESTS",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 0.8.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0EA5E9).copy(alpha = 0.28f))
                                .border(1.dp, BentoSky, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "TAP TO OPEN!",
                                fontSize = 10.sp,
                                color = Color(0xFFBAE6FD),
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChestSlotItem("Wooden", "READY", ChestType.WOODEN, floatY * 0.35f, Modifier.weight(1f)) {
                            openedChestType = ChestType.WOODEN
                            showChestDialog = true
                        }
                        ChestSlotItem("Silver", "READY", ChestType.SILVER, -floatY * 0.35f, Modifier.weight(1f)) {
                            openedChestType = ChestType.SILVER
                            showChestDialog = true
                        }
                        ChestSlotItem("Gold", "READY", ChestType.GOLD, floatY * 0.35f, Modifier.weight(1f)) {
                            openedChestType = ChestType.GOLD
                            showChestDialog = true
                        }
                        ChestSlotItem("Blitz", "READY", ChestType.BLITZ, -floatY * 0.35f, Modifier.weight(1f)) {
                            openedChestType = ChestType.BLITZ
                            showChestDialog = true
                        }
                    }
                }
            }
        }

        // 7. RANKED ARENA & DAILY CHALLENGES
        item {
            Section3DHeader(
                title = "⚔️ ARENA & CHALLENGES",
                badge = "BONUS LOOT",
                accentColor = BentoSky
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeCard(
                    title = LocalizationManager.getString("ranked_battle", lang),
                    desc = "Climb Bronze → Legend",
                    tag = "+35 Trophies",
                    tagColor = RoyalGoldLight,
                    gradientColors = listOf(Color(0xFFB45309), Color(0xFF78350F)),
                    borderColors = listOf(RoyalGoldLight, BentoAmber),
                    showTrophyIcon = true,
                    modifier = Modifier.weight(1f)
                ) {
                    onStartMatch(GameMode.RANKED)
                }

                ModeCard(
                    title = LocalizationManager.getString("practice_mode", lang),
                    desc = "Zero cost vs Smart AI",
                    tag = "🛡️ Unlimited",
                    tagColor = Color(0xFF86EFAC),
                    gradientColors = listOf(Color(0xFF047857), Color(0xFF0F766E)),
                    borderColors = listOf(Color(0xFF6EE7B7), BentoSky),
                    showTrophyIcon = false,
                    modifier = Modifier.weight(1f)
                ) {
                    onStartMatch(GameMode.PRACTICE)
                }
            }
        }

        item {
            ModeCard(
                title = LocalizationManager.getString("daily_challenge", lang),
                desc = "Double capture bonus modifiers today! Earn extra Duck Coins & Gems.",
                tag = "⭐ +150 Duck Coins + Gems",
                tagColor = Color(0xFFFDA4AF),
                gradientColors = listOf(Color(0xFFBE185D), Color(0xFF6D28D9)),
                borderColors = listOf(RoyalGoldLight, Color(0xFFF472B6)),
                showTrophyIcon = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                onStartMatch(GameMode.DAILY_CHALLENGE)
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun ChestSlotItem(
    title: String,
    status: String,
    chestType: ChestType,
    bobOffset: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accentColor = Color(chestType.colorHex)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = accentColor)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF4338CA), Color(0xFF1E293B))
                )
            )
            .border(1.5.dp, accentColor.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Game3DChestCanvas(
            chestType = chestType,
            size = 46.dp,
            modifier = Modifier.offset(y = bobOffset.dp)
        )
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BentoEmerald.copy(alpha = 0.25f))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(status, fontSize = 8.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun LargeModeCard(
    title: String,
    desc: String,
    tag: String,
    tagColor: Color,
    iconEmoji: String,
    gradientColors: List<Color>,
    badgeColors: List<Color>,
    floatOffset: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Game3DCard(
        modifier = modifier.clickable { onClick() },
        gradientColors = gradientColors,
        borderColors = listOf(tagColor, Color.White.copy(alpha = 0.65f)),
        shadowColor = gradientColors.first(),
        cornerRadius = 20.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.42f))
                        .border(1.dp, tagColor.copy(alpha = 0.7f), RoundedCornerShape(9.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = tagColor
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .offset(y = (floatOffset * 0.4f).dp)
                        .shadow(8.dp, CircleShape, ambientColor = badgeColors.first())
                        .clip(CircleShape)
                        .background(Brush.linearGradient(badgeColors))
                        .border(1.8.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(iconEmoji, fontSize = 18.sp)
                }
            }
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF1F5F9),
                lineHeight = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    desc: String,
    tag: String,
    tagColor: Color,
    gradientColors: List<Color>,
    borderColors: List<Color>,
    showTrophyIcon: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Game3DCard(
        modifier = modifier.clickable { onClick() },
        gradientColors = gradientColors,
        borderColors = borderColors,
        shadowColor = gradientColors.first(),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                if (showTrophyIcon) {
                    Game3DTrophyCanvas(size = 16.dp)
                }
                Text(
                    text = tag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = tagColor
                )
            }
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFF1F5F9),
                lineHeight = 15.sp
            )
        }
    }
}
