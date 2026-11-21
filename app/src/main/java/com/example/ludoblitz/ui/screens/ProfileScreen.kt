package com.example.ludoblitz.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ludoblitz.model.CosmeticRegistry
import com.example.ludoblitz.model.LevelMilestone
import com.example.ludoblitz.model.LevelProgression
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.ui.components.Game3DAvatarBadge
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.Game3DTrophyCanvas
import com.example.ludoblitz.ui.components.HdDuckCoinIcon
import com.example.ludoblitz.ui.components.HdGemIcon
import com.example.ludoblitz.ui.components.Section3DHeader
import com.example.ludoblitz.ui.components.profileFrameRingColors
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

@Composable
fun ProfileScreen(
    profile: PlayerProfile,
    onClaimMilestone: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val avatarEmoji = CosmeticRegistry.getItem(profile.equippedAvatar)?.previewEmoji ?: "🦸"
    val frameColors = profileFrameRingColors(profile.equippedProfileFrame)
    val xpFraction = profile.xpProgressFraction.coerceIn(0.06f, 1f)
    val nextLeague = profile.currentLeague.nextTier()
    val leagueSpan = ((nextLeague?.minTrophies ?: profile.currentLeague.maxTrophies) - profile.currentLeague.minTrophies)
        .coerceAtLeast(1)
    val leagueProgress = if (nextLeague == null) 1f else {
        ((profile.trophies - profile.currentLeague.minTrophies).toFloat() / leagueSpan).coerceIn(0.06f, 1f)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Section3DHeader(
                title = "🦸 PLAYER PROFILE",
                badge = "LV ${profile.level} • ${profile.rankTitle.uppercase()}",
                accentColor = RoyalGoldLight
            )
        }

        // Royal 3D Hero Profile Medallion Card
        item {
            Game3DCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_hero_card"),
                gradientColors = listOf(Color(0xFF5B21B6), Color(0xFF312E81), Color(0xFF1D4ED8)),
                borderColors = listOf(RoyalGoldLight, BentoSky, RoyalGoldLight),
                cornerRadius = 24.dp,
                borderWidth = 2.4.dp,
                shadowColor = BentoAmber
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Game3DAvatarBadge(
                        emoji = avatarEmoji,
                        level = profile.level,
                        size = 86.dp,
                        ringColors = frameColors
                    )

                    Text(
                        text = profile.username,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.6.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Rank & League Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF4C1D95).copy(alpha = 0.85f))
                                .border(1.4.dp, BentoSky, RoundedCornerShape(14.dp))
                                .padding(horizontal = 11.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(profile.rankBadgeEmoji, fontSize = 14.sp)
                            Text(
                                text = profile.rankTitle.uppercase(),
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD),
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E1B4B).copy(alpha = 0.72f))
                                .border(1.4.dp, RoyalGoldLight, RoundedCornerShape(14.dp))
                                .padding(horizontal = 11.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Game3DTrophyCanvas(size = 16.dp)
                            Text(
                                text = "${profile.trophies} • ${profile.currentLeague.title}",
                                fontSize = 11.sp,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Glossy Level XP Progress Bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (profile.level >= LevelProgression.MAX_LEVEL) "MAX LEVEL REACHED" else "LEVEL ${profile.level} → ${profile.level + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFBAE6FD)
                            )
                            Text(
                                text = "${profile.xp} / ${profile.xpToNextLevel} XP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = RoyalGoldLight
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF1E1B4B))
                                .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(5.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = xpFraction)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(Brush.horizontalGradient(listOf(RoyalGoldLight, BentoAmber, BentoEmerald)))
                            )
                        }
                    }

                    // League Ladder Progress toward Next League
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "LEAGUE: ${profile.currentLeague.title.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(profile.currentLeague.badgeColorHex)
                            )
                            Text(
                                text = if (nextLeague != null) {
                                    "${(nextLeague.minTrophies - profile.trophies).coerceAtLeast(0)} 🏆 to ${nextLeague.title}"
                                } else {
                                    "APEX LEGEND!"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFE0E7FF)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E1B4B))
                                .border(0.8.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = leagueProgress)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(profile.currentLeague.badgeColorHex),
                                                RoyalGoldLight
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Level Milestones Track (Levels 1 → 100)
        item {
            Section3DHeader(
                title = "🌟 LEVEL MILESTONES",
                badge = if (profile.unclaimedMilestoneCount > 0) "${profile.unclaimedMilestoneCount} READY!" else "${profile.claimedMilestones.size}/${LevelProgression.MILESTONES.size} CLAIMED",
                accentColor = RoyalGoldLight
            )
        }

        items(LevelProgression.MILESTONES, key = { it.level }) { milestone ->
            MilestoneCard(
                milestone = milestone,
                playerLevel = profile.level,
                isClaimed = profile.claimedMilestones.contains(milestone.level),
                onClaim = { onClaimMilestone(milestone.level) }
            )
        }

        // Career Statistics Grid
        item {
            Section3DHeader(
                title = "📊 CAREER STATISTICS",
                badge = "ALL MODES",
                accentColor = BentoSky
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Total Battles",
                    value = "${profile.stats.totalMatches}",
                    emoji = "🎮",
                    gradientColors = listOf(Color(0xFF2563EB), Color(0xFF1E3A8A)),
                    accentColor = BentoSky,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Victories",
                    value = "${profile.stats.matchesWon}",
                    emoji = "👑",
                    gradientColors = listOf(Color(0xFFB45309), Color(0xFF78350F)),
                    accentColor = RoyalGoldLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Win Rate",
                    value = "${profile.stats.winRatePercent}%",
                    emoji = "📈",
                    gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46)),
                    accentColor = Color(0xFF86EFAC),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Best Streak",
                    value = "${profile.stats.highestWinStreak}",
                    emoji = "🔥",
                    gradientColors = listOf(Color(0xFFBE123C), Color(0xFF881337)),
                    accentColor = Color(0xFFFDA4AF),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Captures",
                    value = "${profile.stats.totalCaptures}",
                    emoji = "💥",
                    gradientColors = listOf(Color(0xFF7E22CE), Color(0xFF4C1D95)),
                    accentColor = Color(0xFFE9D5FF),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Pawns Home",
                    value = "${profile.stats.tokensFinished}",
                    emoji = "🏁",
                    gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0C4A6E)),
                    accentColor = Color(0xFFBAE6FD),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MilestoneCard(
    milestone: LevelMilestone,
    playerLevel: Int,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    val isUnlocked = playerLevel >= milestone.level
    val canClaim = isUnlocked && !isClaimed
    val bonusCosmeticName = milestone.rewardCosmeticId?.let { CosmeticRegistry.getItem(it)?.name }

    Game3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_card_${milestone.level}"),
        gradientColors = when {
            isClaimed -> listOf(Color(0xFF283066), Color(0xFF1E1B4B))
            canClaim -> listOf(Color(0xFF047857), Color(0xFF065F46))
            else -> listOf(Color(0xFF3730A3), Color(0xFF1E3A8A))
        },
        borderColors = when {
            isClaimed -> listOf(Color(0xFF64748B), Color(0xFF475569))
            canClaim -> listOf(RoyalGoldLight, Color(0xFF86EFAC))
            else -> listOf(Color(0xFF60A5FA).copy(alpha = 0.7f), RoyalGoldLight.copy(alpha = 0.55f))
        },
        cornerRadius = 18.dp,
        shadowColor = if (canClaim) BentoEmerald else BentoPurple
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(6.dp, CircleShape, ambientColor = RoyalGoldLight)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            if (isUnlocked) listOf(RoyalGoldLight, BentoAmber, RoyalGoldDark)
                            else listOf(Color(0xFF475569), Color(0xFF1E293B))
                        )
                    )
                    .border(1.8.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(milestone.badgeEmoji, fontSize = 21.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "LV ${milestone.level} • ${milestone.rankTitle}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        HdDuckCoinIcon(size = 14.dp, animated = false)
                        Text(
                            text = "+${milestone.rewardCoins}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = RoyalGoldLight
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        HdGemIcon(size = 13.dp, animated = false)
                        Text(
                            text = "+${milestone.rewardGems}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoSky
                        )
                    }
                }

                if (bonusCosmeticName != null) {
                    Text(
                        text = "✨ Unlocks: $bonusCosmeticName",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFDE68A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            when {
                isClaimed -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "✓ CLAIMED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFCBD5E1),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                canClaim -> {
                    Game3DButton(
                        onClick = onClaim,
                        topColor = RoyalGoldLight,
                        bottomColor = BentoAmber,
                        ledgeColor = RoyalGoldDark,
                        height = 40.dp,
                        cornerRadius = 13.dp,
                        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("claim_milestone_${milestone.level}")
                    ) {
                        Text(
                            text = "CLAIM",
                            color = Color(0xFF1E1B4B),
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E1B4B).copy(alpha = 0.72f))
                            .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🔒 LV ${milestone.level}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFBAE6FD),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    emoji: String,
    gradientColors: List<Color>,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Game3DCard(
        modifier = modifier,
        gradientColors = gradientColors,
        borderColors = listOf(accentColor, Color.White.copy(alpha = 0.55f)),
        cornerRadius = 18.dp,
        shadowColor = gradientColors.first()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .shadow(6.dp, CircleShape, ambientColor = accentColor)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A).copy(alpha = 0.35f))
                    .border(1.4.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 19.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = value,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = label,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
