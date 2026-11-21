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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludoblitz.model.CosmeticItem
import com.example.ludoblitz.model.CosmeticRarity
import com.example.ludoblitz.model.CosmeticRegistry
import com.example.ludoblitz.model.CosmeticType
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.ui.components.Game3DButton
import com.example.ludoblitz.ui.components.Game3DCard
import com.example.ludoblitz.ui.components.HdDuckCoinIcon
import com.example.ludoblitz.ui.components.HdGemIcon
import com.example.ludoblitz.ui.components.Section3DHeader
import com.example.ui.theme.BentoAmber
import com.example.ui.theme.BentoEmerald
import com.example.ui.theme.BentoPurple
import com.example.ui.theme.BentoSky
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight

private data class ShopCategoryTab(
    val type: CosmeticType,
    val emoji: String,
    val label: String
)

private val SHOP_TABS = listOf(
    ShopCategoryTab(CosmeticType.DICE_SKIN, "🎲", "Dice"),
    ShopCategoryTab(CosmeticType.TOKEN_SKIN, "🛡️", "Pawns"),
    ShopCategoryTab(CosmeticType.BOARD_THEME, "🌆", "Boards"),
    ShopCategoryTab(CosmeticType.AVATAR, "🦸", "Avatars"),
    ShopCategoryTab(CosmeticType.PROFILE_FRAME, "✨", "Frames"),
    ShopCategoryTab(CosmeticType.VICTORY_EFFECT, "🎆", "Effects")
)

@Composable
fun ShopScreen(
    profile: PlayerProfile,
    onBuyCosmetic: (String, Int, Int) -> Unit,
    onEquipCosmetic: (String, CosmeticType) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val activeTab = SHOP_TABS[selectedCategoryIndex]
    val allItems = remember { CosmeticRegistry.getAllItems() }
    val ownedCount = remember(profile.unlockedCosmetics) {
        allItems.count { profile.unlockedCosmetics.contains(it.id) || (it.costCoins == 0 && it.costGems == 0) }
    }
    val categoryItems = remember(activeTab.type) {
        allItems.filter { it.type == activeTab.type }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("shop_screen_root"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Section3DHeader(
                title = "🛍️ ROYAL MARKETPLACE",
                badge = "$ownedCount/${allItems.size} OWNED",
                accentColor = RoyalGoldLight
            )
        }

        // Vibrant Jewel Marketplace Wallet & Collection Summary Card
        item {
            val collectionFraction = (ownedCount.toFloat() / allItems.size.coerceAtLeast(1)).coerceIn(0.05f, 1f)
            Game3DCard(
                modifier = Modifier.fillMaxWidth(),
                gradientColors = listOf(Color(0xFF4C1D95), Color(0xFF312E81), Color(0xFF1D4ED8)),
                borderColors = listOf(RoyalGoldLight, BentoSky, RoyalGoldLight),
                cornerRadius = 20.dp,
                borderWidth = 2.dp,
                shadowColor = BentoPurple
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "COLLECTOR VAULT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = RoyalGoldLight,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Earn Coins & Gems in Matches, Quests & Wheel",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE0E7FF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF451A03).copy(alpha = 0.85f))
                                    .border(1.2.dp, RoyalGoldLight, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                HdDuckCoinIcon(size = 15.dp, animated = false)
                                Text("${profile.coins}", color = RoyalGoldLight, fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                            }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0C4A6E).copy(alpha = 0.85f))
                                    .border(1.2.dp, BentoSky, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                HdGemIcon(size = 15.dp, animated = false)
                                Text("${profile.gems}", color = Color(0xFFE0F2FE), fontWeight = FontWeight.Black, fontSize = 11.5.sp)
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0F172A).copy(alpha = 0.65f))
                            .border(0.8.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = collectionFraction)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(RoyalGoldLight, BentoAmber, BentoEmerald)))
                        )
                    }
                }
            }
        }

        // Clean Scrollable Category Tabs (Zero Right-Edge Clipping!)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("shop_category_tabs"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                itemsIndexed(SHOP_TABS) { index, tab ->
                    val isSelected = selectedCategoryIndex == index
                    Box(
                        modifier = Modifier
                            .shadow(
                                elevation = if (isSelected) 10.dp else 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = if (isSelected) RoyalGoldLight else Color(0xFF1E3A8A)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (isSelected) listOf(RoyalGoldLight, BentoAmber)
                                    else listOf(Color(0xFF3730A3), Color(0xFF1E1B4B))
                                )
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.2.dp,
                                color = if (isSelected) Color.White else Color(0xFF60A5FA).copy(alpha = 0.6f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedCategoryIndex = index }
                            .padding(horizontal = 13.dp, vertical = 10.dp)
                            .testTag("shop_tab_${tab.type.name.lowercase()}")
                    ) {
                        Text(
                            text = "${tab.emoji} ${tab.label}",
                            color = if (isSelected) Color(0xFF1E1B4B) else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        items(categoryItems, key = { it.id }) { item ->
            CosmeticShopCard(
                item = item,
                profile = profile,
                onBuy = onBuyCosmetic,
                onEquip = onEquipCosmetic
            )
        }

        item {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun CosmeticShopCard(
    item: CosmeticItem,
    profile: PlayerProfile,
    onBuy: (String, Int, Int) -> Unit,
    onEquip: (String, CosmeticType) -> Unit
) {
    val isOwned = profile.unlockedCosmetics.contains(item.id) || (item.costCoins == 0 && item.costGems == 0)
    val isEquipped = when (item.type) {
        CosmeticType.DICE_SKIN -> profile.equippedDiceSkin == item.id
        CosmeticType.TOKEN_SKIN -> profile.equippedTokenSkin == item.id
        CosmeticType.BOARD_THEME -> profile.equippedBoardTheme == item.id
        CosmeticType.AVATAR -> profile.equippedAvatar == item.id
        CosmeticType.PROFILE_FRAME -> profile.equippedProfileFrame == item.id
        CosmeticType.VICTORY_EFFECT -> profile.equippedVictoryFx == item.id
    }

    val rarityAccent = Color(item.rarity.accentColorHex)
    val rarityBg = Color(item.rarity.badgeBgHex)

    val (cardColors, borderColors) = when {
        isEquipped -> listOf(Color(0xFF047857), Color(0xFF064E3B)) to listOf(Color(0xFF86EFAC), RoyalGoldLight)
        item.rarity == CosmeticRarity.MYTHIC -> listOf(Color(0xFF701A3C), Color(0xFF3B0764)) to listOf(Color(0xFFFF2E63), RoyalGoldLight)
        item.rarity == CosmeticRarity.LEGENDARY -> listOf(Color(0xFF78350F), Color(0xFF451A03)) to listOf(RoyalGoldLight, BentoAmber)
        item.rarity == CosmeticRarity.EPIC -> listOf(Color(0xFF5B21B6), Color(0xFF2E1065)) to listOf(Color(0xFFC084FC), Color(0xFF60A5FA))
        item.rarity == CosmeticRarity.RARE -> listOf(Color(0xFF1D4ED8), Color(0xFF1E3A8A)) to listOf(Color(0xFF34D399), Color(0xFF38BDF8))
        else -> listOf(Color(0xFF3730A3), Color(0xFF1E3A8A)) to listOf(Color(0xFF60A5FA), RoyalGoldLight.copy(alpha = 0.7f))
    }

    Game3DCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_item_${item.id}"),
        gradientColors = cardColors,
        borderColors = borderColors,
        cornerRadius = 20.dp,
        borderWidth = if (isEquipped || item.rarity == CosmeticRarity.MYTHIC || item.rarity == CosmeticRarity.LEGENDARY) 2.2.dp else 1.6.dp,
        shadowColor = if (isEquipped) BentoEmerald else rarityAccent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Glossy 3D Item Preview Pedestal with Rarity Glow
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = rarityAccent, spotColor = rarityAccent)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(Color(item.primaryColorHex).copy(alpha = 0.72f), Color(0xFF0F172A))
                        )
                    )
                    .border(2.dp, rarityAccent, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.previewEmoji, fontSize = 29.sp)
            }

            // Item Name, Rarity Pill & Description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(rarityBg)
                            .border(1.dp, rarityAccent, RoundedCornerShape(7.dp))
                            .padding(horizontal = 6.dp, vertical = 1.5.dp)
                    ) {
                        Text(
                            text = item.rarity.displayName.uppercase(),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = rarityAccent,
                            letterSpacing = 0.6.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    if (isOwned && !isEquipped) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color(0xFF065F46))
                                .border(1.dp, Color(0xFF6EE7B7), RoundedCornerShape(7.dp))
                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "OWNED",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFA7F3D0),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Text(
                    text = item.name,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = item.description,
                    fontSize = 11.sp,
                    color = Color(0xFFE0E7FF),
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Action CTA (Guaranteed content-wrapped width — zero card explosion!)
            when {
                isEquipped -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF10B981))
                            .border(1.5.dp, Color.White, RoundedCornerShape(13.dp))
                            .padding(horizontal = 11.dp, vertical = 9.dp)
                            .testTag("shop_equipped_${item.id}")
                    ) {
                        Text(
                            text = "✓ EQUIPPED",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.5.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                isOwned -> {
                    Game3DButton(
                        onClick = { onEquip(item.id, item.type) },
                        topColor = Color(0xFF38BDF8),
                        bottomColor = Color(0xFF0284C7),
                        ledgeColor = Color(0xFF0C4A6E),
                        height = 44.dp,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("shop_equip_${item.id}")
                    ) {
                        Text(
                            text = "EQUIP",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
                else -> {
                    val canAfford = if (item.costGems > 0) profile.gems >= item.costGems else profile.coins >= item.costCoins
                    Game3DButton(
                        onClick = { onBuy(item.id, item.costCoins, item.costGems) },
                        topColor = if (item.costGems > 0) Color(0xFF38BDF8) else RoyalGoldLight,
                        bottomColor = if (item.costGems > 0) Color(0xFF2563EB) else BentoAmber,
                        ledgeColor = if (item.costGems > 0) Color(0xFF1E3A8A) else RoyalGoldDark,
                        borderColor = if (canAfford) Color.White else Color.White.copy(alpha = 0.45f),
                        height = 44.dp,
                        cornerRadius = 14.dp,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("shop_buy_${item.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (item.costGems > 0) {
                                HdGemIcon(size = 16.dp, animated = false)
                                Text(
                                    text = "${item.costGems}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            } else {
                                HdDuckCoinIcon(size = 16.dp, animated = false)
                                Text(
                                    text = "${item.costCoins}",
                                    color = Color(0xFF1E1B4B),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
