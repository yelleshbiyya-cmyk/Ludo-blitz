package com.example.ludoblitz.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludoblitz.model.PassTier
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.SeasonPassManager
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.HdGemIcon
import com.example.ludoblitz.ui.components.Section3DHeader
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoRose
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

@Composable
fun SeasonPassScreen(
    profile: PlayerProfile,
    onUnlockVipPass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tiers = SeasonPassManager.getDefaultTiers()
    val maxTier = tiers.maxOf { it.tierLevel }
    val currentTier = SeasonPassManager.calculateCurrentTier(profile.seasonXp)
    val progressFraction = ((profile.seasonXp % 100).toFloat() / 100f).coerceIn(0f, 1f)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Section3DHeader(
                title = "👑 SEASON 1: ROYAL BLITZ ARENA",
                badge = "TIER $currentTier / $maxTier",
                accentColor = RoyalGoldLight
            )
        }

        // Royal Season Progress & VIP Unlock Banner
        item {
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF5B21B6), Color(0xFF312E81), Color(0xFF1E3A8A)),
                borderColors = listOf(RoyalGoldLight, BentoAmber, RoyalGoldLight),
                cornerRadius = 24.dp,
                borderWidth = 2.4.dp,
                shadowColor = BentoAmber
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Current Tier: $currentTier / $maxTier",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                "Total Season XP: ${profile.seasonXp} ⚡",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBAE6FD)
                            )
                        }
                        if (profile.isSeasonPassPremium) {
                            Box(
                                modifier = Modifier
                                    .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = RoyalGoldLight)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.horizontalGradient(listOf(RoyalGoldLight, BentoAmber, BentoRose)))
                                    .border(1.5.dp, Color.White, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("👑 VIP ACTIVE", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color(0xFF1E1B4B))
                            }
                        }
                    }

                    // Glossy 3D Season XP Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1B4B))
                            .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progressFraction.coerceAtLeast(0.05f))
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(RoyalGoldLight, BentoAmber, BentoEmerald)))
                        )
                    }

                    if (!profile.isSeasonPassPremium) {
                        Game3DButton(
                            onClick = onUnlockVipPass,
                            topColor = RoyalGoldLight,
                            bottomColor = BentoAmber,
                            ledgeColor = RoyalGoldDark,
                            height = 50.dp,
                            cornerRadius = 16.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    "👑 UNLOCK VIP PASS • 100",
                                    color = Color(0xFF1E1B4B),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp
                                )
                                HdGemIcon(size = 18.dp, animated = true)
                            }
                        }
                    }
                }
            }
        }

        item {
            Section3DHeader(
                title = "🎁 PASS TIERS & REWARDS",
                badge = "FREE + VIP",
                accentColor = BentoSky
            )
        }

        items(tiers) { tier ->
            PassTierCard(
                tier = tier,
                isUnlocked = currentTier >= tier.tierLevel,
                isVip = profile.isSeasonPassPremium
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun PassTierCard(
    tier: PassTier,
    isUnlocked: Boolean,
    isVip: Boolean
) {
    Game3DCard(
        modifier = Modifier.fillMaxWidth(),
        gradientColors = if (isUnlocked) {
            listOf(Color(0xFF4338CA), Color(0xFF1E3A8A))
        } else {
            listOf(Color(0xFF2B3475), Color(0xFF1E1B4B))
        },
        borderColors = if (isUnlocked) {
            listOf(RoyalGoldLight, BentoEmerald)
        } else {
            listOf(Color(0xFF60A5FA).copy(alpha = 0.55f), Color(0xFF818CF8).copy(alpha = 0.45f))
        },
        cornerRadius = 18.dp,
        shadowColor = if (isUnlocked) BentoEmerald else BentoPurple
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
                    .size(44.dp)
                    .shadow(8.dp, CircleShape, ambientColor = if (isUnlocked) RoyalGoldLight else Color.Black)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            if (isUnlocked) listOf(RoyalGoldLight, BentoAmber)
                            else listOf(Color(0xFF475569), Color(0xFF1E293B))
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${tier.tierLevel}",
                    fontWeight = FontWeight.Black,
                    color = if (isUnlocked) Color(0xFF1E1B4B) else Color.White,
                    fontSize = 15.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "FREE: ${tier.freeRewardTitle}",
                        fontSize = 12.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )
                    if (isUnlocked) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoEmerald.copy(alpha = 0.28f))
                                .border(1.dp, BentoEmerald, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text("UNLOCKED", fontSize = 9.sp, color = Color(0xFF86EFAC), fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "👑 VIP: ${tier.vipRewardTitle}",
                        fontSize = 12.5.sp,
                        color = RoyalGoldLight,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 6.dp)
                    )
                    if (isUnlocked && isVip) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RoyalGoldLight.copy(alpha = 0.22f))
                                .border(1.dp, RoyalGoldLight, RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text("CLAIMED", fontSize = 9.sp, color = RoyalGoldLight, fontWeight = FontWeight.Black, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}
