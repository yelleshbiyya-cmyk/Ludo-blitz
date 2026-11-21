package com.example.ludoblitz.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
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
    val selectedDiceId: String = "dice_classic",
    val selectedTokenSkinId: String = "token_classic",
    val selectedBoardThemeId: String = "board_neon",
    val selectedVictoryEffectId: String = "fx_confetti",
    val unlockedCosmeticsCsv: String = "dice_classic,token_classic,board_neon,fx_confetti,avatar_blitz_hero,frame_classic",
    val dailyLoginStreak: Int = 1,
    val lastDailyClaimTimestamp: Long = 0L,
    val seasonPassTier: Int = 1,
    val seasonPassXp: Int = 0,
    val isSeasonPassPremium: Boolean = false,
    val hasRemovedAds: Boolean = false,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val languageCode: String = "en",
    val isTutorialCompleted: Boolean = false,
    // Schema v3 additions (safe non-destructive migration)
    val selectedProfileFrameId: String = "frame_classic",
    val musicVolume: Float = 0.7f,
    val sfxVolume: Float = 1.0f,
    val claimedMilestonesCsv: String = "",
    val tokensFinished: Int = 0,
    val weeklyResetTimestamp: Long = 0L
)

@Entity(tableName = "match_history")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val result: String,
    val scorePlayer: Int,
    val scoreOpponent: Int,
    val durationSeconds: Int,
    val playerCaptures: Int,
    val opponentCaptures: Int,
    val winReason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val isDaily: Boolean,
    val currentProgress: Int,
    val isCompleted: Boolean,
    val isClaimed: Boolean
)

@Entity(tableName = "saved_game_state")
data class SavedGameStateEntity(
    @PrimaryKey val id: Int = 1,
    val mode: String,
    val difficulty: String,
    val currentTurn: String,
    val diceValue: Int,
    val hasRolled: Boolean,
    val consecutiveSixes: Int,
    val activePlayersCsv: String,
    val playerTypesCsv: String,
    val isPassAndPlay: Boolean,
    val tokensCsv: String,
    val timestamp: Long = System.currentTimeMillis()
)
