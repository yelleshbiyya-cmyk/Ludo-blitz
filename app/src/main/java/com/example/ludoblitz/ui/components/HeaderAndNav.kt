package com.example.ludoblitz.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludoblitz.model.CosmeticRegistry
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.viewmodel.AppScreen
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoIndigo
import com.example.ui.theme.BentoSky
import com.example.ui.theme.BentoSlateLight
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

@Composable
fun TopHeader(
    profile: PlayerProfile,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedCoins by animateIntAsState(
        targetValue = profile.coins,
        animationSpec = tween(durationMillis = 650),
        label = "coinRoll"
    )
    val animatedGems by animateIntAsState(
        targetValue = profile.gems,
        animationSpec = tween(durationMillis = 650),
        label = "gemRoll"
    )

    var prevCoins by remember { mutableIntStateOf(profile.coins) }
    var prevGems by remember { mutableIntStateOf(profile.gems) }
    val coinScale = remember { Animatable(1f) }
    val gemScale = remember { Animatable(1f) }

    LaunchedEffect(profile.coins) {
        if (profile.coins > prevCoins) {
            coinScale.snapTo(1.24f)
            coinScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
        prevCoins = profile.coins
    }

    LaunchedEffect(profile.gems) {
        if (profile.gems > prevGems) {
            gemScale.snapTo(1.26f)
            gemScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
        prevGems = profile.gems
    }

    val avatarEmoji = remember(profile.equippedAvatar) {
        CosmeticRegistry.getItem(profile.equippedAvatar)?.previewEmoji ?: "🦸"
    }
    val frameColors = remember(profile.equippedProfileFrame) {
        profileFrameRingColors(profile.equippedProfileFrame)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glossy 3D Player Profile Pill (Clean non-overlapping Avatar + Ellipsized Name + XP bar + Trophy)
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .shadow(10.dp, RoundedCornerShape(24.dp), ambientColor = BentoIndigo)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF3949AB), Color(0xFF1E296B))
                    )
                )
                .border(
                    1.6.dp,
                    Brush.linearGradient(frameColors),
                    RoundedCornerShape(24.dp)
                )
                .clickable { onProfileClick() }
                .padding(start = 5.dp, end = 10.dp, top = 4.dp, bottom = 4.dp)
                .testTag("profile_header_pill"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Game3DAvatarBadge(
                emoji = avatarEmoji,
                level = profile.level,
                size = 36.dp,
                ringColors = frameColors
            )
            Column(
                modifier = Modifier.widthIn(min = 56.dp, max = 104.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = profile.username,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Game3DTrophyCanvas(size = 12.dp)
                    Text(
                        text = "${profile.trophies}",
                        color = RoyalGoldLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "• ${profile.rankTitle}",
                        color = Color(0xFFBAE6FD),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Compact live XP micro-bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.7f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = profile.xpProgressFraction.coerceAtLeast(0.06f))
                            .fillMaxSize()
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.horizontalGradient(listOf(RoyalGoldLight, BentoEmerald)))
                    )
                }
            }
        }

        // Glossy 3D Currencies & Tactile 3D Settings Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // 3D Glossy Duck Coins Counter
            Box(
                modifier = Modifier
                    .scale(coinScale.value)
                    .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = BentoAmber, spotColor = BentoAmber)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF78350F), Color(0xFF451A03))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(RoyalGoldLight, BentoAmber)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 5.dp)
                    .testTag("header_coins_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HdDuckCoinIcon(size = 18.dp, animated = true)
                    Text(
                        text = "$animatedCoins",
                        color = RoyalGoldLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // 3D Glossy Diamonds Counter
            Box(
                modifier = Modifier
                    .scale(gemScale.value)
                    .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = BentoSky, spotColor = BentoSky)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0369A1), Color(0xFF0C4A6E))
                        )
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(listOf(Color(0xFFBAE6FD), Color(0xFF38BDF8))),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 5.dp)
                    .testTag("header_gems_pill")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HdGemIcon(size = 18.dp, animated = true)
                    Text(
                        text = "$animatedGems",
                        color = Color(0xFFE0F2FE),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Tactile 3D Settings Button (42dp visual inside generous hit target)
            Game3DButton(
                onClick = onSettingsClick,
                topColor = Color(0xFF6366F1),
                bottomColor = Color(0xFF4338CA),
                ledgeColor = Color(0xFF1E1B4B),
                borderColor = Color(0xFF93C5FD),
                cornerRadius = 13.dp,
                height = 40.dp,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: AppScreen,
    onTabSelected: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp), ambientColor = RoyalGold)
            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF283593), Color(0xFF1A237E))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.2.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(RoyalGoldDark, RoyalGoldLight, RoyalGold, RoyalGoldLight, RoyalGoldDark)
                    )
                )
        )

        NavigationBar(
            modifier = Modifier.navigationBarsPadding(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            val items = listOf(
                Triple(AppScreen.HOME, "Battle", Icons.Default.Home),
                Triple(AppScreen.SHOP, "Shop", Icons.Default.ShoppingBag),
                Triple(AppScreen.SEASON_PASS, "Pass", Icons.Default.Star),
                Triple(AppScreen.QUESTS, "Quests", Icons.Default.CardGiftcard),
                Triple(AppScreen.LEADERBOARD, "Ranks", Icons.Default.EmojiEvents),
                Triple(AppScreen.PROFILE, "Profile", Icons.Default.Person)
            )

            items.forEach { (screen, label, icon) ->
                val isSelected = currentScreen == screen
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.18f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "navIconScale"
                )
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(screen) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) Color(0xFF1E1B4B) else BentoSlateLight.copy(alpha = 0.88f),
                            modifier = Modifier.scale(iconScale)
                        )
                    },
                    label = {
                        Text(
                            text = label.uppercase(),
                            fontSize = 8.5.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) RoyalGoldLight else Color(0xFFBFDBFE),
                            letterSpacing = 0.2.sp,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Clip
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = RoyalGold
                    ),
                    modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                )
            }
        }
    }
}
