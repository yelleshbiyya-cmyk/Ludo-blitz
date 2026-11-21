package com.example.ludoblitz.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ludoblitz.model.ChestType
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

@Composable
fun ChestRewardDialog(
    chestType: ChestType,
    coinsWon: Int,
    gemsWon: Int,
    xpWon: Int,
    onDismiss: () -> Unit
) {
    var popped by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { popped = true }
    val scale by animateFloatAsState(
        targetValue = if (popped) 1f else 0.70f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "chestPop"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "chestSunburst")
    val rayAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Restart),
        label = "rayAngle"
    )

    val chestAccent = Color(chestType.colorHex)

    Dialog(onDismissRequest = onDismiss) {
        Game3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .padding(8.dp),
            gradientColors = listOf(Color(0xFF4338CA), Color(0xFF312E81), Color(0xFF1E1B4B)),
            borderColors = listOf(RoyalGoldLight, chestAccent, RoyalGoldLight),
            cornerRadius = 28.dp,
            borderWidth = 2.6.dp,
            shadowColor = chestAccent
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radiant Sunburst + Sculpted 3D Treasure Chest
                Box(
                    modifier = Modifier.size(124.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension / 2f
                        rotate(degrees = rayAngle, pivot = center) {
                            for (i in 0 until 10) {
                                drawArc(
                                    brush = Brush.radialGradient(
                                        listOf(RoyalGoldLight.copy(alpha = 0.55f), Color.Transparent),
                                        center = center,
                                        radius = r
                                    ),
                                    startAngle = i * 36f,
                                    sweepAngle = 16f,
                                    useCenter = true,
                                    topLeft = Offset.Zero,
                                    size = Size(size.width, size.height)
                                )
                            }
                        }
                    }
                    Game3DChestCanvas(
                        chestType = chestType,
                        size = 96.dp
                    )
                }

                Text(
                    text = "${chestType.title.uppercase()} OPENED!",
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    color = RoyalGoldLight,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF312E81), Color(0xFF1E3A8A))
                            )
                        )
                        .border(1.5.dp, RoyalGoldLight.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HdDuckCoinIcon(size = 34.dp, animated = true)
                        Text("+$coinsWon", fontWeight = FontWeight.Black, color = RoyalGoldLight, fontSize = 15.sp)
                        Text("Duck Coins", fontSize = 10.sp, color = Color(0xFFE0E7FF), fontWeight = FontWeight.Bold)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HdGemIcon(size = 34.dp, animated = true)
                        Text("+$gemsWon", fontWeight = FontWeight.Black, color = BentoSky, fontSize = 15.sp)
                        Text("Gems", fontSize = 10.sp, color = Color(0xFFE0E7FF), fontWeight = FontWeight.Bold)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⚡", fontSize = 28.sp)
                        Text("+$xpWon", fontWeight = FontWeight.Black, color = Color(0xFF86EFAC), fontSize = 15.sp)
                        Text("XP", fontSize = 10.sp, color = Color(0xFFE0E7FF), fontWeight = FontWeight.Bold)
                    }
                }

                Game3DButton(
                    onClick = onDismiss,
                    topColor = RoyalGoldLight,
                    bottomColor = BentoAmber,
                    ledgeColor = RoyalGoldDark,
                    height = 52.dp,
                    cornerRadius = 18.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "COLLECT LOOT!",
                        color = Color(0xFF1E1B4B),
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}
