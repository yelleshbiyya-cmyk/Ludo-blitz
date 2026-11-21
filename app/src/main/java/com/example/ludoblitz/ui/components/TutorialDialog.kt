package com.example.ludoblitz.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

@Composable
fun TutorialDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Game3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("tutorial_dialog"),
            gradientColors = listOf(Color(0xFF4338CA), Color(0xFF312E81), Color(0xFF1E3A8A)),
            borderColors = listOf(RoyalGoldLight, BentoSky, RoyalGoldLight),
            cornerRadius = 26.dp,
            borderWidth = 2.4.dp,
            shadowColor = BentoSky
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Step Progress Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .size(if (i == step) 12.dp else 9.dp)
                                .clip(CircleShape)
                                .background(
                                    if (i == step) RoyalGoldLight
                                    else Color.White.copy(alpha = 0.28f)
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        )
                    }
                }

                when (step) {
                    1 -> {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(12.dp, CircleShape, ambientColor = BentoSky)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFF38BDF8), Color(0xFF1D4ED8))))
                                .border(2.2.dp, RoyalGoldLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎲", fontSize = 38.sp)
                        }
                        Text(
                            text = "60-SECOND BLITZ RULES",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = RoyalGoldLight
                        )
                        Text(
                            text = "• Every Blitz battle finishes in 60 seconds!\n• Tap the 3D dice to roll. Rolling 1 or 6 exits base.\n• Move fast — turns have an 8s timer!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                    2 -> {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(12.dp, CircleShape, ambientColor = BentoAmber)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(RoyalGoldLight, Color(0xFFD97706))))
                                .border(2.2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 38.sp)
                        }
                        Text(
                            text = "POWER-UPS & CAPTURES",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = RoyalGoldLight
                        )
                        Text(
                            text = "• Land on opponent pawns to CAPTURE them (+25 pts)!\n• Golden Stars are SAFE ZONES where you cannot be captured.\n• Trigger Speed Boost, Shield, Double Dice, and Strikes!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                    3 -> {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .shadow(12.dp, CircleShape, ambientColor = RoyalGoldLight)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Color(0xFFEC4899), Color(0xFF7E22CE))))
                                .border(2.2.dp, RoyalGoldLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Game3DTrophyCanvas(size = 44.dp)
                        }
                        Text(
                            text = "ROYAL VICTORY",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = RoyalGoldLight
                        )
                        Text(
                            text = "• Clear all 4 pawns to the center crown to win instantly!\n• At 60 seconds, highest score wins based on finished pawns, board progress, and captures.\n• Fair, fast, and zero waiting!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (step > 1) {
                        Game3DButton(
                            onClick = { step-- },
                            topColor = Color(0xFF6366F1),
                            bottomColor = Color(0xFF4338CA),
                            ledgeColor = Color(0xFF1E1B4B),
                            height = 48.dp,
                            cornerRadius = 16.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BACK", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        }
                    }

                    Game3DButton(
                        onClick = {
                            if (step < 3) {
                                step++
                            } else {
                                onComplete()
                            }
                        },
                        topColor = if (step == 3) RoyalGoldLight else Color(0xFF38BDF8),
                        bottomColor = if (step == 3) BentoAmber else Color(0xFF0284C7),
                        ledgeColor = if (step == 3) RoyalGoldDark else Color(0xFF0C4A6E),
                        height = 48.dp,
                        cornerRadius = 16.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (step == 3) "CLAIM REWARD" else "NEXT ▶",
                            color = if (step == 3) Color(0xFF1E1B4B) else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
