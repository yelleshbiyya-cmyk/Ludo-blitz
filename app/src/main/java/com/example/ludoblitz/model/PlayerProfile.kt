package com.example.ludoblitz.model

data class LevelMilestone(
    val level: Int,
    val rankTitle: String,
    val badgeEmoji: String,
    val rewardCoins: Int,
    val rewardGems: Int,
    val rewardCosmeticId: String? = null
)

object LevelProgression {
    const val MAX_LEVEL = 100

    val MILESTONES: List<LevelMilestone> = listOf(
        LevelMilestone(1, "Beginner", "🌱", 100, 2),
        LevelMilestone(5, "Rookie", "⚡", 250, 5, rewardCosmeticId = "frame_sapphire"),
        LevelMilestone(10, "Challenger", "🛡️", 450, 10, rewardCosmeticId = "dice_neon"),
        LevelMilestone(20, "Expert", "🔥", 800, 18, rewardCosmeticId = "frame_emerald"),
        LevelMilestone(30, "Master", "💎", 1200, 25, rewardCosmeticId = "token_rocket"),
        LevelMilestone(50, "Champion", "🏆", 2000, 45, rewardCosmeticId = "frame_ruby"),
        LevelMilestone(75, "Grandmaster", "🌟", 3500, 80, rewardCosmeticId = "dice_royal_gold"),
        LevelMilestone(100, "Legend", "👑", 6000, 150, rewardCosmeticId = "frame_mythic")
    )

    /**
     * Smooth, balanced XP curve:
     * L1 -> 100 XP, L2 -> 150 XP, L5 -> 300 XP, L10 -> 550 XP, L20 -> 1050 XP...
     */
    fun xpRequiredForLevel(level: Int): Int {
        val safeLevel = level.coerceIn(1, MAX_LEVEL)
        return 100 + (safeLevel - 1) * 50
    }

    fun rankTitleForLevel(level: Int): String {
        return MILESTONES.lastOrNull { level >= it.level }?.rankTitle ?: "Beginner"
    }

    fun badgeForLevel(level: Int): String {
        return MILESTONES.lastOrNull { level >= it.level }?.badgeEmoji ?: "🌱"
    }

    fun nextMilestoneAfter(level: Int): LevelMilestone? {
        return MILESTONES.firstOrNull { it.level > level }
    }

    fun applyXpGain(currentLevel: Int, currentXp: Int, xpGain: Int): Pair<Int, Int> {
        if (xpGain <= 0) return currentLevel.coerceIn(1, MAX_LEVEL) to currentXp.coerceAtLeast(0)
        var level = currentLevel.coerceIn(1, MAX_LEVEL)
        var xp = currentXp.coerceAtLeast(0) + xpGain
        while (level < MAX_LEVEL) {
            val req = xpRequiredForLevel(level)
            if (xp >= req) {
                xp -= req
                level += 1
            } else {
                break
            }
        }
        if (level >= MAX_LEVEL) {
            xp = xp.coerceAtMost(xpRequiredForLevel(MAX_LEVEL))
        }
        return level to xp
    }
}

data class PlayerStatsSummary(
    val totalMatches: Int = 0,
    val matchesWon: Int = 0,
    val matchesLost: Int = 0,
    val currentWinStreak: Int = 0,
    val highestWinStreak: Int = 0,
    val totalCaptures: Int = 0,
    val tokensFinished: Int = 0
) {
    val winRatePercent: Int get() = if (totalMatches == 0) 0 else ((matchesWon * 100) / totalMatches)
}

data class PlayerProfile(
    val username: String = "BlitzWarrior",
    val avatarId: String = "avatar_blitz_hero",
    val level: Int = 1,
    val xp: Int = 0,
    val trophies: Int = 120,
    val coins: Int = 500,
    val gems: Int = 20,
    val wins: Int = 0,
    val losses: Int = 0,
    val winStreak: Int = 0,
    val highestStreak: Int = 0,
    val totalMatches: Int = 0,
    val totalCaptures: Int = 0,
    val tokensFinished: Int = 0,

    // Equipped cosmetics
    val equippedDiceSkin: String = "dice_classic",
    val equippedTokenSkin: String = "token_classic",
    val equippedBoardTheme: String = "board_neon",
    val equippedAvatar: String = "avatar_blitz_hero",
    val equippedProfileFrame: String = "frame_classic",
    val equippedVictoryFx: String = "fx_confetti",

    // Unlocked IDs
    val unlockedCosmetics: Set<String> = setOf(
        "dice_classic",
        "token_classic",
        "board_neon",
        "fx_confetti",
        "avatar_blitz_hero",
        "frame_classic"
    ),

    // Claimed level milestone numbers (e.g., 1, 5, 10)
    val claimedMilestones: Set<Int> = emptySet(),

    // Retention & pass
    val dailyStreakDay: Int = 1,
    val lastDailyClaimTimestamp: Long = 0L,
    val weeklyResetTimestamp: Long = 0L,
    val seasonPassTier: Int = 1,
    val seasonXp: Int = 0,
    val isSeasonPassPremium: Boolean = false,
    val hasRemovedAds: Boolean = false,

    // Audio & Haptics — Background music is OFF by default!
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = false,
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 1.0f,
    val vibrationEnabled: Boolean = true,

    val languageCode: String = "en",
    val isTutorialCompleted: Boolean = false
) {
    val currentLeague: LeagueTier get() = LeagueTier.fromTrophies(trophies)
    val winRate: Int get() = if (totalMatches == 0) 0 else ((wins * 100) / totalMatches)
    val xpToNextLevel: Int get() = LevelProgression.xpRequiredForLevel(level)
    val xpProgressFraction: Float get() = if (level >= LevelProgression.MAX_LEVEL) 1f else (xp.toFloat() / xpToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
    val rankTitle: String get() = LevelProgression.rankTitleForLevel(level)
    val rankBadgeEmoji: String get() = LevelProgression.badgeForLevel(level)
    val unclaimedMilestoneCount: Int
        get() = LevelProgression.MILESTONES.count { level >= it.level && !claimedMilestones.contains(it.level) }

    val stats: PlayerStatsSummary get() = PlayerStatsSummary(
        totalMatches = totalMatches,
        matchesWon = wins,
        matchesLost = losses,
        currentWinStreak = winStreak,
        highestWinStreak = highestStreak,
        totalCaptures = totalCaptures,
        tokensFinished = tokensFinished
    )
}
