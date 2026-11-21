package com.example.ludoblitz.model

data class Quest(
    val id: String,
    val title: String,
    val description: String,
    val currentProgress: Int,
    val targetProgress: Int,
    val rewardCoins: Int,
    val rewardGems: Int = 0,
    val rewardXp: Int = 50,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val badgeEmoji: String = "🎯",
    val difficulty: MissionDifficulty = MissionDifficulty.EASY
)

data class PassTier(
    val tierLevel: Int,
    val requiredXp: Int,
    val freeRewardTitle: String,
    val vipRewardTitle: String,
    val freeCoins: Int = 50,
    val freeGems: Int = 0,
    val vipCoins: Int = 150,
    val vipGems: Int = 10,
    val vipCosmeticId: String? = null
)

object SeasonPassManager {
    fun getDefaultTiers(): List<PassTier> {
        return (1..15).map { tier ->
            PassTier(
                tierLevel = tier,
                requiredXp = tier * 100,
                freeRewardTitle = "+${50 + tier * 20} Coins",
                vipRewardTitle = when (tier) {
                    5 -> "⚡ Neon Cyber Dice"
                    10 -> "🤖 Cyber Mech Pawn"
                    15 -> "👑 Royal Crown Pawn + 50 💎"
                    else -> "+${150 + tier * 40} Coins & +${tier * 2} Gems"
                },
                freeCoins = 50 + tier * 20,
                vipCoins = 150 + tier * 40,
                vipGems = tier * 2,
                vipCosmeticId = when (tier) {
                    5 -> "dice_neon"
                    10 -> "token_robot"
                    15 -> "token_crown"
                    else -> null
                }
            )
        }
    }

    fun calculateCurrentTier(xp: Int): Int {
        return (xp / 100).coerceIn(1, 15)
    }
}

data class DailyLoginReward(
    val day: Int,
    val coins: Int,
    val gems: Int,
    val xp: Int,
    val badgeEmoji: String
)

object DailyRewardCalendar {
    fun getRewards(): List<DailyLoginReward> = listOf(
        DailyLoginReward(1, 100, 2, 35, "🎁"),
        DailyLoginReward(2, 150, 3, 45, "🪙"),
        DailyLoginReward(3, 200, 5, 55, "💎"),
        DailyLoginReward(4, 250, 7, 70, "⚡"),
        DailyLoginReward(5, 300, 10, 85, "🔥"),
        DailyLoginReward(6, 400, 15, 100, "🎲"),
        DailyLoginReward(7, 1000, 30, 180, "👑")
    )
}

object QuestFactory {
    fun getDefaultDailyMissions(): List<Quest> = listOf(
        Quest("daily_play_3", "Battle Master", "Play 3 Ludo Matches", 0, 3, 100, 2, 45, badgeEmoji = "⚔️", difficulty = MissionDifficulty.EASY),
        Quest("daily_win_1", "Speed Champion", "Win 1 Match", 0, 1, 120, 3, 60, badgeEmoji = "🏆", difficulty = MissionDifficulty.EASY),
        Quest("daily_capture_3", "Token Hunter", "Capture 3 opponent pawns", 0, 3, 150, 2, 55, badgeEmoji = "💥", difficulty = MissionDifficulty.MEDIUM),
        Quest("daily_powerup_2", "Blitz Tactician", "Activate 2 Power-Ups in Blitz", 0, 2, 90, 2, 40, badgeEmoji = "⚡", difficulty = MissionDifficulty.EASY),
        Quest("daily_finish_2", "Crown Striker", "Bring 2 pawns to Center Home", 0, 2, 140, 3, 65, badgeEmoji = "🏁", difficulty = MissionDifficulty.MEDIUM)
    )

    fun getDefaultWeeklyMissions(): List<Quest> = listOf(
        Quest("weekly_play_12", "Arena Regular", "Complete 12 Matches this week", 0, 12, 450, 10, 180, badgeEmoji = "🎯", difficulty = MissionDifficulty.MEDIUM),
        Quest("weekly_win_6", "Weekly Victor", "Win 6 Matches this week", 0, 6, 650, 15, 240, badgeEmoji = "👑", difficulty = MissionDifficulty.HARD),
        Quest("weekly_capture_15", "Board Crusher", "Capture 15 opponent pawns", 0, 15, 550, 12, 200, badgeEmoji = "💣", difficulty = MissionDifficulty.MEDIUM),
        Quest("weekly_finish_10", "Home Runner", "Guide 10 pawns into Home Crown", 0, 10, 700, 18, 260, badgeEmoji = "🌟", difficulty = MissionDifficulty.HARD)
    )

    fun getDefaultAchievements(): List<Quest> = listOf(
        Quest("ach_first_win", "First Victory", "Achieve your 1st Victory", 0, 1, 200, 10, 100, badgeEmoji = "🥇", difficulty = MissionDifficulty.EASY),
        Quest("ach_matches_10", "Seasoned Roller", "Complete 10 Matches", 0, 10, 300, 12, 140, badgeEmoji = "🎲", difficulty = MissionDifficulty.EASY),
        Quest("ach_10_wins", "Veteran Roller", "Win 10 Matches", 0, 10, 500, 25, 250, badgeEmoji = "🎖️", difficulty = MissionDifficulty.MEDIUM),
        Quest("ach_50_wins", "Ludo Gladiator", "Win 50 Matches", 0, 50, 1500, 60, 600, badgeEmoji = "👑", difficulty = MissionDifficulty.HARD),
        Quest("ach_100_wins", "Blitz Sovereign", "Win 100 Matches", 0, 100, 3000, 150, 1200, badgeEmoji = "🌌", difficulty = MissionDifficulty.HARD),
        Quest("ach_streak_5", "Unstoppable Force", "Reach a 5-Win Streak", 0, 5, 450, 20, 220, badgeEmoji = "🔥", difficulty = MissionDifficulty.MEDIUM),
        Quest("ach_streak_10", "Legendary Momentum", "Reach a 10-Win Streak", 0, 10, 1200, 50, 500, badgeEmoji = "⚡", difficulty = MissionDifficulty.HARD),
        Quest("ach_captures_25", "Token Shredder", "Capture 25 Opponent Pawns", 0, 25, 600, 30, 300, badgeEmoji = "💣", difficulty = MissionDifficulty.MEDIUM),
        Quest("ach_captures_100", "Board Dominator", "Capture 100 Opponent Pawns", 0, 100, 2000, 100, 900, badgeEmoji = "💥", difficulty = MissionDifficulty.HARD),
        Quest("ach_level_5", "Rising Rookie", "Reach Player Level 5", 0, 5, 350, 15, 120, badgeEmoji = "⭐", difficulty = MissionDifficulty.EASY),
        Quest("ach_level_10", "Elite Challenger", "Reach Player Level 10", 0, 10, 750, 30, 250, badgeEmoji = "🛡️", difficulty = MissionDifficulty.MEDIUM),
        Quest("ach_cosmetics_5", "Style Collector", "Unlock 8 Cosmetic Collectibles", 0, 8, 500, 20, 200, badgeEmoji = "🎨", difficulty = MissionDifficulty.MEDIUM),
        Quest("ach_league_gold", "Golden Tier", "Reach Gold League (700 Trophies)", 0, 700, 800, 40, 400, badgeEmoji = "🏆", difficulty = MissionDifficulty.MEDIUM),
        Quest("ach_league_legend", "Apex Legend", "Ascend to Legend League (3000 Trophies)", 0, 3000, 5000, 300, 1800, badgeEmoji = "🌟", difficulty = MissionDifficulty.HARD)
    )
}
