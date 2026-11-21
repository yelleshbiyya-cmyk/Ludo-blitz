package com.example.ludoblitz.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ludoblitz.model.Quest
import com.example.ludoblitz.viewmodel.LevelUpCelebrationEvent
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import kotlinx.coroutines.delay

@Composable
fun LevelUpCelebrationDialog(
    event: LevelUpCelebrationEvent,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lvlUpAnim")
    val sunburstAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "lvlSunburst"
    )
    val badgePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "lvlPulse"
    )

    Dialog(onDismissRequest = onDismiss) {
        Game3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .testTag("level_up_dialog"),
            gradientColors = listOf(Color(0xFF5B21B6), Color(0xFF312E81), Color(0xFF1E3A8A)),
            borderColors = listOf(RoyalGoldLight, BentoAmber, RoyalGoldLight),
            cornerRadius = 26.dp,
            borderWidth = 2.6.dp,
            shadowColor = BentoAmber
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "✨ LEVEL UP! ✨",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = RoyalGoldLight,
                    letterSpacing = 1.4.sp
                )

                Box(
                    modifier = Modifier.size(126.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension / 2f
                        rotate(sunburstAngle, pivot = center) {
                            for (i in 0 until 12) {
                                drawArc(
                                    color = RoyalGoldLight.copy(alpha = 0.22f),
                                    startAngle = i * 30f,
                                    sweepAngle = 14f,
                                    useCenter = true,
                                    topLeft = Offset.Zero,
                                    size = Size(r * 2f, r * 2f)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(badgePulse)
                            .shadow(16.dp, CircleShape, ambientColor = RoyalGoldLight)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(RoyalGoldLight, BentoAmber, RoyalGoldDark)))
                            .border(3.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(event.badgeEmoji, fontSize = 28.sp)
                            Text(
                                text = "LV ${event.newLevel}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1E1B4B)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E1B4B).copy(alpha = 0.72f))
                        .border(1.4.dp, BentoSky, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "RANK: ${event.rankTitle.uppercase()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFBAE6FD),
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = if (event.unlockedMilestoneLevel != null) {
                        "🎁 Level ${event.unlockedMilestoneLevel} Milestone Reward Ready to Claim in Profile!"
                    } else {
                        "Awesome progression! Keep winning battles and missions to unlock exclusive milestones."
                    },
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Game3DButton(
                    onClick = onDismiss,
                    topColor = Color(0xFF4ADE80),
                    bottomColor = Color(0xFF16A34A),
                    ledgeColor = Color(0xFF14532D),
                    height = 48.dp,
                    cornerRadius = 16.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("level_up_dismiss_button")
                ) {
                    Text(
                        text = "AWESOME!",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementUnlockedBanner(
    achievement: Quest?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(achievement?.id) {
        if (achievement != null) {
            delay(3800)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = achievement != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        val ach = achievement ?: return@AnimatedVisibility
        Game3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onDismiss() }
                .testTag("achievement_unlocked_banner"),
            gradientColors = listOf(Color(0xFF047857), Color(0xFF065F46), Color(0xFF1E3A8A)),
            borderColors = listOf(RoyalGoldLight, BentoEmerald),
            cornerRadius = 18.dp,
            borderWidth = 2.dp,
            shadowColor = BentoEmerald
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(RoyalGoldLight, BentoAmber)))
                        .border(1.8.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(ach.badgeEmoji, fontSize = 22.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "🏆 ACHIEVEMENT UNLOCKED!",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = RoyalGoldLight,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = ach.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Claim +${ach.rewardCoins} Coins & +${ach.rewardGems} Gems in Quests!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBBF7D0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
