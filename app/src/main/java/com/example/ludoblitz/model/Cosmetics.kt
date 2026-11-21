package com.example.ludoblitz.model

data class CosmeticItem(
    val id: String,
    val name: String,
    val type: CosmeticType,
    val costCoins: Int = 0,
    val costGems: Int = 0,
    val isPremium: Boolean = false,
    val previewEmoji: String = "🎲",
    val description: String = "",
    val primaryColorHex: Long = 0xFF00E5FF,
    val rarity: CosmeticRarity = CosmeticRarity.COMMON
)

object CosmeticRegistry {
    val DICE_SKINS = listOf(
        CosmeticItem("dice_classic", "Classic Ivory", CosmeticType.DICE_SKIN, 0, 0, false, "🎲", "Crisp glossy ivory dice with deep onyx pips", 0xFFE2E8F0, CosmeticRarity.COMMON),
        CosmeticItem("dice_neon", "Neon Cyber", CosmeticType.DICE_SKIN, 350, 0, false, "⚡", "Electric cyan and violet glowing arcade cube", 0xFF00E5FF, CosmeticRarity.RARE),
        CosmeticItem("dice_fire", "Inferno Flame", CosmeticType.DICE_SKIN, 600, 0, false, "🔥", "Molten ruby core with golden spark embers", 0xFFFF3D00, CosmeticRarity.EPIC),
        CosmeticItem("dice_ice", "Frost Crystal", CosmeticType.DICE_SKIN, 800, 0, false, "❄️", "Faceted glacier diamond with sapphire sheen", 0xFF38BDF8, CosmeticRarity.EPIC),
        CosmeticItem("dice_royal_gold", "Royal Sovereign", CosmeticType.DICE_SKIN, 1400, 0, false, "👑", "24K hammered gold cube inlaid with jewels", 0xFFFFD700, CosmeticRarity.LEGENDARY),
        CosmeticItem("dice_galaxy", "Cosmic Galaxy", CosmeticType.DICE_SKIN, 0, 30, true, "🌌", "Starlight nebula cube forged in mythic space", 0xFFC084FC, CosmeticRarity.MYTHIC)
    )

    val TOKEN_SKINS = listOf(
        CosmeticItem("token_classic", "Royal Jewels", CosmeticType.TOKEN_SKIN, 0, 0, false, "🛡️", "Emerald Shield, Gold Crown, Ruby Flame & Sapphire", 0xFF00E5FF, CosmeticRarity.COMMON),
        CosmeticItem("token_robot", "Cyber Mech", CosmeticType.TOKEN_SKIN, 400, 0, false, "🤖", "Armored neon combat droid squad", 0xFF10B981, CosmeticRarity.RARE),
        CosmeticItem("token_rocket", "Star Rocket", CosmeticType.TOKEN_SKIN, 650, 0, false, "🚀", "Supersonic gold-tipped star cruisers", 0xFFF59E0B, CosmeticRarity.EPIC),
        CosmeticItem("token_monster", "Shadow Beast", CosmeticType.TOKEN_SKIN, 900, 0, false, "👾", "Arcade plasma guardians with bright aura", 0xFFEC4899, CosmeticRarity.EPIC),
        CosmeticItem("token_crown", "Royal Majesty", CosmeticType.TOKEN_SKIN, 0, 40, true, "👑", "Solid gold jeweled crown pawns", 0xFFFFD700, CosmeticRarity.LEGENDARY),
        CosmeticItem("token_phoenix", "Mythic Phoenix", CosmeticType.TOKEN_SKIN, 0, 65, true, "🦅", "Radiant solar phoenix crest pawns", 0xFFFF2E63, CosmeticRarity.MYTHIC)
    )

    val BOARD_THEMES = listOf(
        CosmeticItem("board_neon", "Royal Arcade", CosmeticType.BOARD_THEME, 0, 0, false, "🌆", "Deep sapphire stadium with radiant jewel glow", 0xFF2563EB, CosmeticRarity.COMMON),
        CosmeticItem("board_classic", "Grand Pavilion", CosmeticType.BOARD_THEME, 250, 0, false, "🛡️", "Polished gold-trimmed tournament board", 0xFF10B981, CosmeticRarity.RARE),
        CosmeticItem("board_space", "Deep Space", CosmeticType.BOARD_THEME, 700, 0, false, "🪐", "Celestial violet cosmos with starfield shine", 0xFF7C3AED, CosmeticRarity.EPIC),
        CosmeticItem("board_jungle", "Emerald Temple", CosmeticType.BOARD_THEME, 950, 0, false, "🌴", "Vibrant emerald ruins with gilded tiles", 0xFF059669, CosmeticRarity.EPIC),
        CosmeticItem("board_desert", "Solar Palace", CosmeticType.BOARD_THEME, 1200, 0, false, "🏜️", "Sun-drenched amber and gold oasis arena", 0xFFF59E0B, CosmeticRarity.LEGENDARY),
        CosmeticItem("board_cyber", "Mythic Prism", CosmeticType.BOARD_THEME, 0, 50, true, "💠", "Holographic crystal prism championship deck", 0xFFFF2E63, CosmeticRarity.MYTHIC)
    )

    val AVATARS = listOf(
        CosmeticItem("avatar_blitz_hero", "Blitz Hero", CosmeticType.AVATAR, 0, 0, false, "🦸", "Fearless champion of the Royal Blitz arena", 0xFF00E5FF, CosmeticRarity.COMMON),
        CosmeticItem("avatar_cyber_ninja", "Cyber Ninja", CosmeticType.AVATAR, 200, 0, false, "🥷", "Lightning-fast stealth tactician", 0xFFEC4899, CosmeticRarity.RARE),
        CosmeticItem("avatar_speed_runner", "Volt Striker", CosmeticType.AVATAR, 350, 0, false, "⚡", "Always first across the home crown", 0xFFF59E0B, CosmeticRarity.RARE),
        CosmeticItem("avatar_grandmaster", "Grandmaster", CosmeticType.AVATAR, 600, 0, false, "🧙", "Sees six turns ahead on every roll", 0xFF8B5CF6, CosmeticRarity.EPIC),
        CosmeticItem("avatar_dragon", "Crimson Dragon", CosmeticType.AVATAR, 1100, 0, false, "🐉", "Blazing guardian of the ruby vault", 0xFFFF2E63, CosmeticRarity.LEGENDARY),
        CosmeticItem("avatar_robo_king", "Sovereign King", CosmeticType.AVATAR, 0, 25, true, "👑", "Crowned ruler of the Legend league", 0xFFFFD700, CosmeticRarity.MYTHIC)
    )

    val PROFILE_FRAMES = listOf(
        CosmeticItem("frame_classic", "Royal Gold Ring", CosmeticType.PROFILE_FRAME, 0, 0, false, "⭕", "Signature polished gold medallion ring", 0xFFFFD700, CosmeticRarity.COMMON),
        CosmeticItem("frame_sapphire", "Sapphire Halo", CosmeticType.PROFILE_FRAME, 300, 0, false, "💠", "Electric cyan & sapphire luminous rim", 0xFF00E5FF, CosmeticRarity.RARE),
        CosmeticItem("frame_emerald", "Emerald Laurel", CosmeticType.PROFILE_FRAME, 550, 0, false, "🌿", "Crystalline emerald champion crest border", 0xFF10B981, CosmeticRarity.EPIC),
        CosmeticItem("frame_ruby", "Ruby Inferno", CosmeticType.PROFILE_FRAME, 950, 0, false, "🔥", "Blazing crimson aura with golden sparks", 0xFFFF2E63, CosmeticRarity.LEGENDARY),
        CosmeticItem("frame_mythic", "Mythic Crown Frame", CosmeticType.PROFILE_FRAME, 0, 35, true, "✨", "Prismatic violet-gold Grandmaster frame", 0xFFC084FC, CosmeticRarity.MYTHIC)
    )

    val VICTORY_EFFECTS = listOf(
        CosmeticItem("fx_confetti", "Confetti Rain", CosmeticType.VICTORY_EFFECT, 0, 0, false, "🎉", "Vibrant festival ribbons & golden sparkle", 0xFF38BDF8, CosmeticRarity.COMMON),
        CosmeticItem("fx_lightning", "Lightning Strike", CosmeticType.VICTORY_EFFECT, 450, 0, false, "⚡", "Electric cyan thunderbolts on victory", 0xFF00E5FF, CosmeticRarity.RARE),
        CosmeticItem("fx_fireworks", "Grand Fireworks", CosmeticType.VICTORY_EFFECT, 800, 0, false, "🎆", "Royal stadium fireworks finale", 0xFFFF6D00, CosmeticRarity.EPIC),
        CosmeticItem("fx_royal_crown", "Golden Crown Rain", CosmeticType.VICTORY_EFFECT, 1300, 0, false, "👑", "Cascading golden crowns & Duck Coins", 0xFFFFD700, CosmeticRarity.LEGENDARY),
        CosmeticItem("fx_energy_burst", "Cosmic Supernova", CosmeticType.VICTORY_EFFECT, 0, 35, true, "💥", "Radiant mythic shockwave & gem storm", 0xFFFF2E63, CosmeticRarity.MYTHIC)
    )

    fun getAllItems(): List<CosmeticItem> =
        DICE_SKINS + TOKEN_SKINS + BOARD_THEMES + AVATARS + PROFILE_FRAMES + VICTORY_EFFECTS

    fun getItem(id: String): CosmeticItem? = getAllItems().firstOrNull { it.id == id }
}
