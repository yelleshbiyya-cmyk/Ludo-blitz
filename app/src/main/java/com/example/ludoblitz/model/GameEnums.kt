package com.example.ludoblitz.model

enum class PlayerSide(
    val colorHex: Long,
    val displayName: String,
    val startTile: Int,
    val homeGateTile: Int
) {
    RED(0xFFE11D48, "Red", 0, 50),
    GREEN(0xFF10B981, "Green", 13, 11),
    YELLOW(0xFFF59E0B, "Yellow", 26, 24),
    BLUE(0xFF3B82F6, "Blue", 39, 37);

    companion object {
        val PLAYER get() = RED
        val OPPONENT get() = YELLOW
    }
}

enum class PlayerType {
    HUMAN,
    AI
}

enum class ClassicPlayerCount(val count: Int) {
    TWO_PLAYERS(2),
    FOUR_PLAYERS(4)
}

data class ClassicMatchConfig(
    val playerCount: ClassicPlayerCount = ClassicPlayerCount.TWO_PLAYERS,
    val isPassAndPlay: Boolean = false,
    val playerTypes: Map<PlayerSide, PlayerType> = mapOf(
        PlayerSide.RED to PlayerType.HUMAN,
        PlayerSide.YELLOW to PlayerType.AI,
        PlayerSide.GREEN to PlayerType.AI,
        PlayerSide.BLUE to PlayerType.AI
    )
)

enum class TokenState {
    BASE,
    TRACK,
    HOME_STRETCH,
    FINISHED
}

enum class PowerUpType(
    val titleKey: String,
    val descKey: String,
    val costCoins: Int = 50,
    val maxChargesPerMatch: Int = 1
) {
    SPEED_BOOST("powerup_speed_title", "powerup_speed_desc", 40, 2),
    SHIELD("powerup_shield_title", "powerup_shield_desc", 50, 1),
    DOUBLE_DICE("powerup_double_title", "powerup_double_desc", 60, 1),
    EXTRA_TURN("powerup_extra_title", "powerup_extra_desc", 75, 1),
    SAFE_STEP("powerup_safe_title", "powerup_safe_desc", 65, 1),
    BLITZ_MOVE("powerup_blitz_title", "powerup_blitz_desc", 80, 1)
}

enum class GameMode(val titleKey: String, val entryCost: Int, val trophyReward: Int, val coinReward: Int) {
    QUICK_BATTLE("mode_quick_title", 50, 20, 100),
    RANKED("mode_ranked_title", 100, 35, 200),
    PRACTICE("mode_practice_title", 0, 0, 25),
    DAILY_CHALLENGE("mode_daily_title", 0, 25, 150),
    CLASSIC("mode_classic_title", 0, 10, 75)
}

enum class LeagueTier(
    val title: String,
    val minTrophies: Int,
    val maxTrophies: Int,
    val badgeColorHex: Long
) {
    BRONZE("Bronze", 0, 299, 0xFFCD7F32),
    SILVER("Silver", 300, 699, 0xFFC0C0C0),
    GOLD("Gold", 700, 1199, 0xFFFFD700),
    PLATINUM("Platinum", 1200, 1799, 0xFF00E5FF),
    DIAMOND("Diamond", 1800, 2399, 0xFF00B0FF),
    MASTER("Master", 2400, 2999, 0xFFE040FB),
    LEGEND("Legend", 3000, 99999, 0xFFFF1744);

    fun nextTier(): LeagueTier? {
        val idx = entries.indexOf(this)
        return entries.getOrNull(idx + 1)
    }

    companion object {
        fun fromTrophies(trophies: Int): LeagueTier {
            return entries.lastOrNull { trophies >= it.minTrophies } ?: BRONZE
        }
    }
}

enum class CosmeticType {
    DICE_SKIN,
    TOKEN_SKIN,
    BOARD_THEME,
    AVATAR,
    PROFILE_FRAME,
    VICTORY_EFFECT
}

enum class CosmeticRarity(
    val displayName: String,
    val accentColorHex: Long,
    val badgeBgHex: Long
) {
    COMMON("Common", 0xFF38BDF8, 0xFF0C4A6E),
    RARE("Rare", 0xFF10B981, 0xFF064E3B),
    EPIC("Epic", 0xFFC084FC, 0xFF4C1D95),
    LEGENDARY("Legendary", 0xFFFFD700, 0xFF78350F),
    MYTHIC("Mythic", 0xFFFF2E63, 0xFF701A3C)
}

enum class MissionDifficulty(
    val label: String,
    val colorHex: Long
) {
    EASY("Easy", 0xFF10B981),
    MEDIUM("Medium", 0xFFF59E0B),
    HARD("Hard", 0xFFFF2E63)
}

enum class AIDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class Language(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    TELUGU("te", "Telugu", "తెలుగు"),
    HINDI("hi", "Hindi", "हिन्दी")
}

enum class BattleResult {
    VICTORY,
    DEFEAT,
    DRAW
}

enum class ChestType(
    val title: String,
    val unlockTimeSeconds: Int,
    val minCoins: Int,
    val maxCoins: Int,
    val minGems: Int,
    val maxGems: Int,
    val minXp: Int,
    val maxXp: Int,
    val colorHex: Long
) {
    WOODEN("Wooden Chest", 0, 40, 80, 1, 3, 20, 40, 0xFF8D6E63),
    SILVER("Silver Chest", 1800, 100, 200, 3, 8, 50, 100, 0xFFB0BEC5),
    GOLD("Gold Chest", 7200, 250, 500, 10, 25, 120, 250, 0xFFFFD54F),
    BLITZ("Blitz Chest", 14400, 600, 1200, 25, 60, 300, 600, 0xFFE040FB)
}
