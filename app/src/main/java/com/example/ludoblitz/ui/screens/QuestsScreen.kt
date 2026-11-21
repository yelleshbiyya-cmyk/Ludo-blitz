package com.example.ludoblitz.ui.screens

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
import com.example.ludoblitz.model.DailyLoginReward
import com.example.ludoblitz.model.DailyRewardCalendar
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.Quest
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.HdDuckCoinIcon
import com.example.ludoblitz.ui.components.HdGemIcon
import com.example.ludoblitz.ui.components.Section3DHeader
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldLight

@Composable
fun QuestsScreen(
    profile: PlayerProfile,
    dailyMissions: List<Quest>,
    weeklyMissions: List<Quest> = emptyList(),
    achievements: List<Quest>,
    onClaimDailyReward: (day: Int, coins: Int, gems: Int, xp: Int) -> Unit,
    onClaimQuest: (Quest, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyRewards = DailyRewardCalendar.getRewards()
    val now = System.currentTimeMillis()
    val elapsed = if (profile.lastDailyClaimTimestamp == 0L) Long.MAX_VALUE else (now - profile.lastDailyClaimTimestamp)
    val canClaimToday = profile.lastDailyClaimTimestamp == 0L || elapsed >= 20L * 60L * 60L * 1000L
    val streakWillReset = profile.lastDailyClaimTimestamp > 0L && elapsed >= 48L * 60L * 60L * 1000L
    val activeDay = if (streakWillReset) 1 else profile.dailyStreakDay.coerceIn(1, 7)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp)
            .testTag("quests_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Section3DHeader(
                title = "🎁 MISSIONS & CALENDAR",
                badge = "DAY $activeDay STREAK",
                accentColor = RoyalGoldLight
            )
        }

        // 7-Day Royal Login Calendar Card
        item {
            Game3DCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_calendar_card"),
                gradientColors = listOf(Color(0xFF4338CA), Color(0xFF312E81), Color(0xFF1E3A8A)),
                borderColors = listOf(RoyalGoldLight, BentoAmber, RoyalGoldLight),
                cornerRadius = 22.dp,
                borderWidth = 2.2.dp,
                shadowColor = BentoAmber
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
                        Text(
                            text = "📅 7-DAY LOGIN REWARDS",
                            fontWeight = FontWeight.Black,
                            color = RoyalGoldLight,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = when {
                                streakWillReset -> "RESTARTS DAY 1"
                                canClaimToday -> "TAP DAY $activeDay!"
                                else -> "CLAIMED TODAY"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (canClaimToday) Color(0xFF86EFAC) else Color(0xFFBAE6FD),
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        for (r in dailyRewards) {
                            DailyRewardItem(
                                reward = r,
                                currentStreak = activeDay,
                                canClaimToday = canClaimToday,
                                modifier = Modifier.weight(1f),
                                onClaim = {
                                    onClaimDailyReward(r.day, r.coins, r.gems, r.xp)
                                }
                            )
                        }
                    }

                    Text(
                        text = "Log in daily to climb to Day 7 (+1000 Coins, +30 Gems, +180 XP). Missing a day resets the streak.",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE0E7FF),
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Daily Missions
        item {
            Section3DHeader(
                title = "⚡ DAILY MISSIONS",
                badge = "${dailyMissions.count { it.isClaimed }}/${dailyMissions.size} DONE",
                accentColor = BentoSky
            )
        }

        items(dailyMissions, key = { it.id }) { quest ->
            QuestItemCard(quest = quest, onClaim = { onClaimQuest(quest, true) })
        }

        // Weekly Missions
        if (weeklyMissions.isNotEmpty()) {
            item {
                Section3DHeader(
                    title = "🗓️ WEEKLY MISSIONS",
                    badge = "${weeklyMissions.count { it.isClaimed }}/${weeklyMissions.size} DONE",
                    accentColor = Color(0xFFC084FC)
                )
            }

            items(weeklyMissions, key = { it.id }) { quest ->
                QuestItemCard(quest = quest, onClaim = { onClaimQuest(quest, true) })
            }
        }

        // Achievements
        item {
            Section3DHeader(
                title = "🏆 ACHIEVEMENTS",
                badge = "${achievements.count { it.isClaimed }}/${achievements.size} UNLOCKED",
                accentColor = RoyalGoldLight
            )
        }

        items(achievements, key = { it.id }) { quest ->
            QuestItemCard(quest = quest, onClaim = { onClaimQuest(quest, false) })
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DailyRewardItem(
    reward: DailyLoginReward,
    currentStreak: Int,
    canClaimToday: Boolean,
    modifier: Modifier = Modifier,
    onClaim: () -> Unit
) {
    val isToday = reward.day == currentStreak
    val isClaimable = isToday && canClaimToday
    val isPassed = reward.day < currentStreak

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
            .shadow(if (isClaimable) 8.dp else 2.dp, RoundedCornerShape(12.dp), ambientColor = RoyalGoldLight)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    when {
                        isClaimable -> listOf(RoyalGoldLight, BentoAmber)
                        isPassed -> listOf(Color(0xFF047857), Color(0xFF065F46))
                        else -> listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
                    }
                )
            )
            .border(
                width = if (isClaimable) 2.dp else 1.dp,
                color = when {
                    isClaimable -> Color.White
                    isPassed -> Color(0xFF86EFAC)
                    else -> Color(0xFF60A5FA).copy(alpha = 0.5f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isClaimable) { onClaim() }
            .padding(vertical = 8.dp, horizontal = 2.dp)
            .testTag("daily_reward_day_${reward.day}")
    ) {
        Text(
            text = "D${reward.day}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = if (isClaimable) Color(0xFF1E1B4B) else Color(0xFFE0E7FF),
            maxLines = 1,
            softWrap = false
        )
        Text(reward.badgeEmoji, fontSize = 15.sp)
        Text(
            text = when {
                isPassed -> "✅"
                isClaimable -> "CLAIM"
                isToday -> "DONE"
                else -> "+${reward.coins}"
            },
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = if (isClaimable) Color(0xFF1E1B4B) else Color.White,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun QuestItemCard(
    quest: Quest,
    onClaim: () -> Unit
) {
    val isComplete = quest.currentProgress >= quest.targetProgress
    val progressFraction = (quest.currentProgress.toFloat() / quest.targetProgress.coerceAtLeast(1)).coerceIn(0f, 1f)
    val diffColor = Color(quest.difficulty.colorHex)

    Game3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quest_card_${quest.id}"),
        gradientColors = when {
            quest.isClaimed -> listOf(Color(0xFF283066), Color(0xFF1E1B4B))
            isComplete -> listOf(Color(0xFF047857), Color(0xFF065F46))
            else -> listOf(Color(0xFF3730A3), Color(0xFF1E3A8A))
        },
        borderColors = when {
            quest.isClaimed -> listOf(Color(0xFF64748B), Color(0xFF475569))
            isComplete -> listOf(RoyalGoldLight, Color(0xFF86EFAC))
            else -> listOf(Color(0xFF60A5FA), RoyalGoldLight.copy(alpha = 0.7f))
        },
        cornerRadius = 18.dp,
        shadowColor = if (isComplete) BentoEmerald else BentoPurple
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(6.dp, CircleShape, ambientColor = RoyalGoldLight)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF6366F1), Color(0xFF1E1B4B))))
                    .border(1.8.dp, RoyalGoldLight.copy(alpha = 0.85f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(quest.badgeEmoji, fontSize = 21.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = quest.title,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(diffColor.copy(alpha = 0.25f))
                            .border(0.9.dp, diffColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = quest.difficulty.label.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = diffColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Text(
                    text = quest.description,
                    fontSize = 11.sp,
                    color = Color(0xFFE0E7FF),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )

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
                            .fillMaxWidth(fraction = progressFraction.coerceAtLeast(if (quest.currentProgress > 0) 0.06f else 0f))
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    if (isComplete) listOf(RoyalGoldLight, BentoEmerald)
                                    else listOf(Color(0xFF38BDF8), RoyalGoldLight)
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${quest.currentProgress} / ${quest.targetProgress}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGoldLight
                    )
                    Text(
                        text = "⚡ +${quest.rewardXp} XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF86EFAC)
                    )
                }
            }

            if (quest.isClaimed) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "✓ DONE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else if (isComplete) {
                Game3DButton(
                    onClick = onClaim,
                    topColor = Color(0xFF4ADE80),
                    bottomColor = Color(0xFF16A34A),
                    ledgeColor = Color(0xFF14532D),
                    height = 42.dp,
                    cornerRadius = 14.dp,
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("claim_quest_${quest.id}")
                ) {
                    Text(
                        text = "CLAIM",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.5.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("+${quest.rewardCoins}", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = RoyalGoldLight)
                        HdDuckCoinIcon(size = 15.dp, animated = false)
                    }
                    if (quest.rewardGems > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("+${quest.rewardGems}", fontSize = 10.5.sp, fontWeight = FontWeight.Black, color = BentoSky)
                            HdGemIcon(size = 14.dp, animated = false)
                        }
                    }
                }
            }
        }
    }
}
