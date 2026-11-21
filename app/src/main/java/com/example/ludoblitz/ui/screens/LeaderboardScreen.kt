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
import com.example.ludoblitz.model.CosmeticRegistry
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.service.LeaderboardEntry
import com.example.ludoblitz.ui.components.Game3DAvatarBadge
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.Game3DTrophyCanvas
import com.example.ludoblitz.ui.components.Section3DHeader
import com.example.ludoblitz.ui.components.profileFrameRingColors
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

@Composable
fun LeaderboardScreen(
    profile: PlayerProfile,
    entries: List<LeaderboardEntry>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Section3DHeader(
                title = "🏆 ARENA LEADERBOARD",
                badge = profile.currentLeague.title.uppercase(),
                accentColor = RoyalGoldLight
            )
        }

        // Current User Royal Standing Banner
        item {
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF5B21B6), Color(0xFF312E81), Color(0xFF1D4ED8)),
                borderColors = listOf(RoyalGoldLight, BentoAmber, RoyalGoldLight),
                cornerRadius = 22.dp,
                borderWidth = 2.4.dp,
                shadowColor = BentoAmber
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val avatarEmoji = CosmeticRegistry.getItem(profile.equippedAvatar)?.previewEmoji ?: "🦸"
                        Game3DAvatarBadge(
                            emoji = avatarEmoji,
                            level = profile.level,
                            size = 52.dp,
                            ringColors = profileFrameRingColors(profile.equippedProfileFrame)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                "${profile.username} (You)",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${profile.currentLeague.title} Arena • ${profile.rankTitle}",
                                fontSize = 11.5.sp,
                                color = RoyalGoldLight,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E1B4B).copy(alpha = 0.7f))
                            .border(1.4.dp, RoyalGoldLight, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Game3DTrophyCanvas(size = 20.dp)
                        Text(
                            "${profile.trophies}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = RoyalGoldLight
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(2.dp))
            Section3DHeader(
                title = "👑 TOP ARENA PLAYERS",
                badge = "GLOBAL RANKS",
                accentColor = BentoSky
            )
        }

        items(entries) { entry ->
            LeaderboardCard(entry)
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun LeaderboardCard(entry: LeaderboardEntry) {
    val medalGradient = when (entry.rank) {
        1 -> listOf(RoyalGoldLight, BentoAmber, RoyalGoldDark)
        2 -> listOf(Color(0xFFF8FAFC), Color(0xFF94A3B8), Color(0xFF475569))
        3 -> listOf(Color(0xFFFDBA74), Color(0xFFD97706), Color(0xFF78350F))
        else -> listOf(Color(0xFF4F46E5), Color(0xFF312E81))
    }

    val cardGradient = when {
        entry.isCurrentUser -> listOf(Color(0xFF047857), Color(0xFF065F46))
        entry.rank == 1 -> listOf(Color(0xFF78350F), Color(0xFF451A03))
        entry.rank == 2 -> listOf(Color(0xFF334155), Color(0xFF1E293B))
        entry.rank == 3 -> listOf(Color(0xFF581C87), Color(0xFF312E81))
        else -> listOf(Color(0xFF3730A3), Color(0xFF1E3A8A))
    }

    val borderColors = when {
        entry.isCurrentUser -> listOf(Color(0xFF86EFAC), RoyalGoldLight)
        entry.rank == 1 -> listOf(RoyalGoldLight, BentoAmber)
        entry.rank == 2 -> listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
        entry.rank == 3 -> listOf(Color(0xFFFDBA74), Color(0xFFF59E0B))
        else -> listOf(Color(0xFF60A5FA).copy(alpha = 0.65f), Color(0xFF818CF8).copy(alpha = 0.5f))
    }

    Game3DCard(
        modifier = Modifier.fillMaxWidth(),
        gradientColors = cardGradient,
        borderColors = borderColors,
        cornerRadius = 18.dp,
        shadowColor = if (entry.rank <= 3) BentoAmber else BentoPurple
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3D Podium Rank Medallion
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(6.dp, CircleShape, ambientColor = medalGradient.first())
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(medalGradient))
                        .border(1.5.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.rank}",
                        fontWeight = FontWeight.Black,
                        color = if (entry.rank <= 3) Color(0xFF1E1B4B) else Color.White,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1B4B).copy(alpha = 0.65f))
                        .border(1.2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(entry.avatarEmoji, fontSize = 20.sp)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        entry.name,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        entry.league,
                        fontSize = 11.sp,
                        color = Color(0xFFE0E7FF),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0xFF1E1B4B).copy(alpha = 0.55f))
                    .border(1.dp, RoyalGoldLight.copy(alpha = 0.6f), RoundedCornerShape(11.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Game3DTrophyCanvas(size = 16.dp)
                Text("${entry.trophies}", fontWeight = FontWeight.Black, color = RoyalGoldLight, fontSize = 13.sp)
            }
        }
    }
}
