package com.example.ludoblitz.data

import com.example.ludoblitz.model.AIDifficulty
import com.example.ludoblitz.model.BattleResult
import com.example.ludoblitz.model.CosmeticType
import com.example.ludoblitz.model.DailyRewardCalendar
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.model.GameState
import com.example.ludoblitz.model.LevelMilestone
import com.example.ludoblitz.model.LevelProgression
import com.example.ludoblitz.model.MatchStats
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.model.PlayerType
import com.example.ludoblitz.model.Quest
import com.example.ludoblitz.model.QuestFactory
import com.example.ludoblitz.model.SeasonPassManager
import com.example.ludoblitz.model.Token
import com.example.ludoblitz.model.TokenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class XpAwardResult(
    val oldLevel: Int,
    val newLevel: Int,
    val xpAdded: Int,
    val newlyCompletedAchievements: List<Quest> = emptyList()
) {
    val leveledUp: Boolean get() = newLevel > oldLevel
}

data class MatchEndResult(
    val xpAwardResult: XpAwardResult,
    val earnedCoins: Int,
    val earnedXp: Int
)

data class DailyClaimResult(
    val success: Boolean,
    val resetStreak: Boolean = false,
    val claimedDay: Int = 1,
    val coinsAwarded: Int = 0,
    val gemsAwarded: Int = 0,
    val xpAwarded: Int = 0,
    val xpAwardResult: XpAwardResult? = null
)

data class QuestClaimResult(
    val success: Boolean,
    val xpAwardResult: XpAwardResult? = null
)

data class MilestoneClaimResult(
    val success: Boolean,
    val milestone: LevelMilestone? = null
)

class GameRepository(
    private val database: AppDatabase
) {
    private val playerDao = database.playerDao()
    private val matchHistoryDao = database.matchHistoryDao()
    private val questDao = database.questDao()
    private val savedGameDao = database.savedGameDao()

    val playerProfileFlow: Flow<PlayerProfile> = playerDao.getPlayerProfileFlow().map { entity ->
        entity?.toDomain() ?: PlayerProfile()
    }

    val recentMatchesFlow = matchHistoryDao.getRecentMatches()

    val savedGameFlow: Flow<GameState?> = savedGameDao.getSavedGame().map { entity ->
        entity?.toDomainGameState()
    }

    suspend fun saveActiveGame(state: GameState) {
        if (state.isGameOver) {
            savedGameDao.clearSavedGame()
        } else {
            savedGameDao.saveGame(state.toSavedEntity())
        }
    }

    suspend fun getSavedGame(): GameState? {
        return savedGameDao.getSavedGameOnce()?.toDomainGameState()
    }

    suspend fun clearSavedGame() {
        savedGameDao.clearSavedGame()
    }

    val dailyMissionsFlow: Flow<List<Quest>> = questDao.getAllQuests().map { questEntities: List<QuestEntity> ->
        mergeQuests(QuestFactory.getDefaultDailyMissions(), questEntities)
    }

    val weeklyMissionsFlow: Flow<List<Quest>> = questDao.getAllQuests().map { questEntities: List<QuestEntity> ->
        mergeQuests(QuestFactory.getDefaultWeeklyMissions(), questEntities)
    }

    val achievementsFlow: Flow<List<Quest>> = questDao.getAllQuests().map { questEntities: List<QuestEntity> ->
        mergeQuests(QuestFactory.getDefaultAchievements(), questEntities)
    }

    private fun mergeQuests(defaults: List<Quest>, savedEntities: List<QuestEntity>): List<Quest> {
        val entityMap = savedEntities.associateBy { it.id }
        return defaults.map { defaultQuest ->
            val saved = entityMap[defaultQuest.id]
            if (saved != null) {
                defaultQuest.copy(
                    currentProgress = saved.currentProgress.coerceAtMost(defaultQuest.targetProgress),
                    isCompleted = saved.currentProgress >= defaultQuest.targetProgress,
                    isClaimed = saved.isClaimed
                )
            } else {
                defaultQuest
            }
        }
    }

    suspend fun initDefaultProfileIfNeeded() {
        val existing = playerDao.getPlayerProfile()
        if (existing == null) {
            playerDao.insertOrUpdateProfile(PlayerProfile().toEntity())
        } else {
            val domain = existing.toDomain()
            if (!domain.unlockedCosmetics.contains("frame_classic")) {
                playerDao.insertOrUpdateProfile(
                    domain.copy(unlockedCosmetics = domain.unlockedCosmetics + "frame_classic").toEntity()
                )
            }
        }
    }

    suspend fun getProfileOnce(): PlayerProfile {
        return playerDao.getPlayerProfile()?.toDomain() ?: PlayerProfile()
    }

    suspend fun updateProfile(profile: PlayerProfile) {
        playerDao.insertOrUpdateProfile(profile.toEntity())
    }

    suspend fun addCurrency(coins: Int, gems: Int) {
        val current = getProfileOnce()
        val updated = current.copy(
            coins = (current.coins + coins).coerceAtLeast(0),
            gems = (current.gems + gems).coerceAtLeast(0)
        )
        playerDao.insertOrUpdateProfile(updated.toEntity())
    }

    suspend fun awardXp(xpGain: Int): XpAwardResult {
        val current = getProfileOnce()
        if (xpGain <= 0) {
            return XpAwardResult(current.level, current.level, 0)
        }
        val (newLevel, newXp) = LevelProgression.applyXpGain(current.level, current.xp, xpGain)
        val newSeasonXp = current.seasonXp + xpGain
        val newSeasonTier = SeasonPassManager.calculateCurrentTier(newSeasonXp)

        val updated = current.copy(
            level = newLevel,
            xp = newXp,
            seasonXp = newSeasonXp,
            seasonPassTier = newSeasonTier
        )
        playerDao.insertOrUpdateProfile(updated.toEntity())

        val newlyUnlocked = syncLevelAndCollectionAchievements(updated)
        return XpAwardResult(
            oldLevel = current.level,
            newLevel = newLevel,
            xpAdded = xpGain,
            newlyCompletedAchievements = newlyUnlocked
        )
    }

    suspend fun claimLevelMilestone(level: Int): MilestoneClaimResult {
        val milestone = LevelProgression.MILESTONES.firstOrNull { it.level == level }
            ?: return MilestoneClaimResult(false)
        val profile = getProfileOnce()
        if (profile.level < level || profile.claimedMilestones.contains(level)) {
            return MilestoneClaimResult(false)
        }
        val unlockedCosmetics = if (milestone.rewardCosmeticId != null) {
            profile.unlockedCosmetics + milestone.rewardCosmeticId
        } else {
            profile.unlockedCosmetics
        }
        val updated = profile.copy(
            coins = profile.coins + milestone.rewardCoins,
            gems = profile.gems + milestone.rewardGems,
            unlockedCosmetics = unlockedCosmetics,
            claimedMilestones = profile.claimedMilestones + level
        )
        playerDao.insertOrUpdateProfile(updated.toEntity())
        syncLevelAndCollectionAchievements(updated)
        return MilestoneClaimResult(true, milestone)
    }

    suspend fun recordMatchEnd(
        mode: GameMode,
        result: BattleResult,
        scorePlayer: Int,
        scoreOpponent: Int,
        durationSeconds: Int,
        stats: MatchStats,
        winReason: String?
    ): MatchEndResult {
        matchHistoryDao.insertMatch(
            MatchRecordEntity(
                mode = mode.name,
                result = result.name,
                scorePlayer = scorePlayer,
                scoreOpponent = scoreOpponent,
                durationSeconds = durationSeconds,
                playerCaptures = stats.playerCaptures,
                opponentCaptures = stats.opponentCaptures,
                winReason = winReason ?: "Match concluded"
            )
        )

        val profile = getProfileOnce()
        val isWin = result == BattleResult.VICTORY
        val isDraw = result == BattleResult.DRAW

        val baseXp = when {
            isWin -> 85
            isDraw -> 45
            else -> 30
        }
        val bonusXp = (stats.playerCaptures * 5) + (stats.playerTokensFinished * 10)
        val earnedXp = baseXp + bonusXp
        val earnedCoins = if (isWin) mode.coinReward else 20
        val deltaTrophies = when {
            mode == GameMode.RANKED && isWin -> mode.trophyReward
            mode == GameMode.RANKED && isDraw -> 0
            mode == GameMode.RANKED -> -15
            mode == GameMode.QUICK_BATTLE && isWin -> 10
            mode == GameMode.CLASSIC && isWin -> 10
            else -> 0
        }

        val newStreak = if (isWin) profile.winStreak + 1 else 0
        val highestStreak = maxOf(profile.highestStreak, newStreak)
        val (newLevel, newXp) = LevelProgression.applyXpGain(profile.level, profile.xp, earnedXp)
        val newSeasonXp = profile.seasonXp + earnedXp

        val updatedProfile = profile.copy(
            level = newLevel,
            xp = newXp,
            trophies = (profile.trophies + deltaTrophies).coerceAtLeast(0),
            coins = profile.coins + earnedCoins,
            wins = if (isWin) profile.wins + 1 else profile.wins,
            losses = if (!isWin && !isDraw) profile.losses + 1 else profile.losses,
            winStreak = newStreak,
            highestStreak = highestStreak,
            totalMatches = profile.totalMatches + 1,
            totalCaptures = profile.totalCaptures + stats.playerCaptures,
            tokensFinished = profile.tokensFinished + stats.playerTokensFinished,
            seasonXp = newSeasonXp,
            seasonPassTier = SeasonPassManager.calculateCurrentTier(newSeasonXp)
        )

        playerDao.insertOrUpdateProfile(updatedProfile.toEntity())

        // Daily & Weekly Mission increments
        incrementQuestProgress("daily_play_3", 1, isDaily = true)
        incrementQuestProgress("weekly_play_12", 1, isDaily = true)
        if (isWin) {
            incrementQuestProgress("daily_win_1", 1, isDaily = true)
            incrementQuestProgress("weekly_win_6", 1, isDaily = true)
        }
        if (stats.playerCaptures > 0) {
            incrementQuestProgress("daily_capture_3", stats.playerCaptures, isDaily = true)
            incrementQuestProgress("weekly_capture_15", stats.playerCaptures, isDaily = true)
        }
        if (stats.playerPowerUpsUsed > 0) {
            incrementQuestProgress("daily_powerup_2", stats.playerPowerUpsUsed, isDaily = true)
        }
        if (stats.playerTokensFinished > 0) {
            incrementQuestProgress("daily_finish_2", stats.playerTokensFinished, isDaily = true)
            incrementQuestProgress("weekly_finish_10", stats.playerTokensFinished, isDaily = true)
        }

        // Sync Achievements accurately from cumulative totals & detect newly unlocked ones
        val newlyUnlocked = mutableListOf<Quest>()
        newlyUnlocked += setAchievementProgress("ach_first_win", updatedProfile.wins)
        newlyUnlocked += setAchievementProgress("ach_matches_10", updatedProfile.totalMatches)
        newlyUnlocked += setAchievementProgress("ach_10_wins", updatedProfile.wins)
        newlyUnlocked += setAchievementProgress("ach_50_wins", updatedProfile.wins)
        newlyUnlocked += setAchievementProgress("ach_100_wins", updatedProfile.wins)
        newlyUnlocked += setAchievementProgress("ach_streak_5", updatedProfile.highestStreak)
        newlyUnlocked += setAchievementProgress("ach_streak_10", updatedProfile.highestStreak)
        newlyUnlocked += setAchievementProgress("ach_captures_25", updatedProfile.totalCaptures)
        newlyUnlocked += setAchievementProgress("ach_captures_100", updatedProfile.totalCaptures)
        newlyUnlocked += syncLevelAndCollectionAchievements(updatedProfile)

        return MatchEndResult(
            xpAwardResult = XpAwardResult(
                oldLevel = profile.level,
                newLevel = newLevel,
                xpAdded = earnedXp,
                newlyCompletedAchievements = newlyUnlocked
            ),
            earnedCoins = earnedCoins,
            earnedXp = earnedXp
        )
    }

    private suspend fun syncLevelAndCollectionAchievements(profile: PlayerProfile): List<Quest> {
        val unlocked = mutableListOf<Quest>()
        unlocked += setAchievementProgress("ach_level_5", profile.level)
        unlocked += setAchievementProgress("ach_level_10", profile.level)
        unlocked += setAchievementProgress("ach_cosmetics_5", profile.unlockedCosmetics.size)
        unlocked += setAchievementProgress("ach_league_gold", profile.trophies)
        unlocked += setAchievementProgress("ach_league_legend", profile.trophies)
        return unlocked
    }

    private suspend fun setAchievementProgress(questId: String, absoluteValue: Int): List<Quest> {
        val template = QuestFactory.getDefaultAchievements().firstOrNull { it.id == questId }
            ?: return emptyList()
        val existing = questDao.getQuestById(questId)
        val prevProgress = existing?.currentProgress ?: 0
        val newProgress = maxOf(prevProgress, absoluteValue).coerceAtMost(template.targetProgress)
        val wasCompleted = prevProgress >= template.targetProgress
        val isNowCompleted = newProgress >= template.targetProgress

        if (existing == null || newProgress != prevProgress || isNowCompleted != existing.isCompleted) {
            questDao.updateQuest(
                QuestEntity(
                    id = questId,
                    isDaily = false,
                    currentProgress = newProgress,
                    isCompleted = isNowCompleted,
                    isClaimed = existing?.isClaimed ?: false
                )
            )
        }
        return if (!wasCompleted && isNowCompleted) {
            listOf(template.copy(currentProgress = newProgress, isCompleted = true))
        } else {
            emptyList()
        }
    }

    suspend fun incrementQuestProgress(questId: String, delta: Int, isDaily: Boolean) {
        if (delta <= 0) return
        val template = (QuestFactory.getDefaultDailyMissions() +
            QuestFactory.getDefaultWeeklyMissions() +
            QuestFactory.getDefaultAchievements()).firstOrNull { it.id == questId }
        val target = template?.targetProgress ?: Int.MAX_VALUE
        val existing = questDao.getQuestById(questId)
        val newProgress = ((existing?.currentProgress ?: 0) + delta).coerceAtMost(target)
        questDao.updateQuest(
            QuestEntity(
                id = questId,
                isDaily = isDaily,
                currentProgress = newProgress,
                isCompleted = newProgress >= target,
                isClaimed = existing?.isClaimed ?: false
            )
        )
    }

    /**
     * Atomically claims a completed mission or achievement with strict duplicate-claim protection.
     */
    suspend fun claimQuestAtomic(quest: Quest, isDaily: Boolean): QuestClaimResult {
        val existing = questDao.getQuestById(quest.id)
        if (existing == null || existing.isClaimed || existing.currentProgress < quest.targetProgress) {
            return QuestClaimResult(false)
        }
        questDao.updateQuest(
            existing.copy(
                currentProgress = quest.targetProgress,
                isCompleted = true,
                isClaimed = true
            )
        )
        addCurrency(quest.rewardCoins, quest.rewardGems)
        val xpRes = awardXp(quest.rewardXp)
        return QuestClaimResult(success = true, xpAwardResult = xpRes)
    }

    suspend fun saveQuestProgress(questId: String, progress: Int, isClaimed: Boolean, isDaily: Boolean) {
        questDao.updateQuest(
            QuestEntity(
                id = questId,
                isDaily = isDaily,
                currentProgress = progress,
                isCompleted = isClaimed,
                isClaimed = isClaimed
            )
        )
    }

    suspend fun unlockCosmetic(cosmeticId: String, costCoins: Int, costGems: Int): Boolean {
        val profile = getProfileOnce()
        // Prevent duplicate purchase or insufficient funds
        if (profile.unlockedCosmetics.contains(cosmeticId)) return false
        if (profile.coins < costCoins || profile.gems < costGems) return false

        val newUnlocked = profile.unlockedCosmetics + cosmeticId
        val updated = profile.copy(
            coins = profile.coins - costCoins,
            gems = profile.gems - costGems,
            unlockedCosmetics = newUnlocked
        )
        playerDao.insertOrUpdateProfile(updated.toEntity())
        syncLevelAndCollectionAchievements(updated)
        return true
    }

    suspend fun equipCosmetic(cosmeticId: String, type: CosmeticType): Boolean {
        val profile = getProfileOnce()
        if (!profile.unlockedCosmetics.contains(cosmeticId)) return false
        val updated = when (type) {
            CosmeticType.DICE_SKIN -> profile.copy(equippedDiceSkin = cosmeticId)
            CosmeticType.TOKEN_SKIN -> profile.copy(equippedTokenSkin = cosmeticId)
            CosmeticType.BOARD_THEME -> profile.copy(equippedBoardTheme = cosmeticId)
            CosmeticType.AVATAR -> profile.copy(equippedAvatar = cosmeticId, avatarId = cosmeticId)
            CosmeticType.PROFILE_FRAME -> profile.copy(equippedProfileFrame = cosmeticId)
            CosmeticType.VICTORY_EFFECT -> profile.copy(equippedVictoryFx = cosmeticId)
        }
        playerDao.insertOrUpdateProfile(updated.toEntity())
        return true
    }

    /**
     * Claims Daily Login Streak reward with:
     * - 20h anti-duplicate claim protection
     * - 48h missed-day reset back to Day 1
     * - XP + Coin + Gem reward persistence
     */
    suspend fun claimDailyReward(
        day: Int,
        coins: Int,
        gems: Int,
        xp: Int = 35,
        nowMs: Long = System.currentTimeMillis()
    ): DailyClaimResult {
        val profile = getProfileOnce()
        val cooldownMs = 20L * 60L * 60L * 1000L
        val missedWindowMs = 48L * 60L * 60L * 1000L

        if (profile.lastDailyClaimTimestamp > 0L && (nowMs - profile.lastDailyClaimTimestamp) < cooldownMs) {
            return DailyClaimResult(success = false, claimedDay = profile.dailyStreakDay)
        }

        val didMissDay = profile.lastDailyClaimTimestamp > 0L &&
            (nowMs - profile.lastDailyClaimTimestamp) > missedWindowMs
        val effectiveDay = if (didMissDay) 1 else profile.dailyStreakDay.coerceIn(1, 7)
        val rewardDef = DailyRewardCalendar.getRewards().firstOrNull { it.day == effectiveDay }
        val awardCoins = rewardDef?.coins ?: coins
        val awardGems = rewardDef?.gems ?: gems
        val awardXpAmount = rewardDef?.xp ?: xp

        val nextStreak = if (effectiveDay >= 7) 1 else effectiveDay + 1
        val updated = profile.copy(
            coins = profile.coins + awardCoins,
            gems = profile.gems + awardGems,
            dailyStreakDay = nextStreak,
            lastDailyClaimTimestamp = nowMs
        )
        playerDao.insertOrUpdateProfile(updated.toEntity())
        val xpRes = awardXp(awardXpAmount)
        return DailyClaimResult(
            success = true,
            resetStreak = didMissDay,
            claimedDay = effectiveDay,
            coinsAwarded = awardCoins,
            gemsAwarded = awardGems,
            xpAwarded = awardXpAmount,
            xpAwardResult = xpRes
        )
    }
}

private val DEFAULT_STARTER_COSMETICS = setOf(
    "dice_classic",
    "token_classic",
    "board_neon",
    "fx_confetti",
    "avatar_blitz_hero",
    "frame_classic"
)

fun PlayerProfileEntity.toDomain(): PlayerProfile {
    val parsedCosmetics = unlockedCosmeticsCsv.split(",").filter { it.isNotBlank() }.toSet()
    val parsedMilestones = claimedMilestonesCsv.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    return PlayerProfile(
        username = username,
        avatarId = avatarId,
        level = level.coerceIn(1, LevelProgression.MAX_LEVEL),
        xp = xp.coerceAtLeast(0),
        trophies = trophies.coerceAtLeast(0),
        coins = coins.coerceAtLeast(0),
        gems = gems.coerceAtLeast(0),
        wins = wins,
        losses = losses,
        winStreak = winStreak,
        highestStreak = highestStreak,
        totalMatches = totalMatches,
        totalCaptures = totalCaptures,
        tokensFinished = tokensFinished,
        equippedDiceSkin = selectedDiceId,
        equippedTokenSkin = selectedTokenSkinId,
        equippedBoardTheme = selectedBoardThemeId,
        equippedAvatar = avatarId,
        equippedProfileFrame = selectedProfileFrameId.ifBlank { "frame_classic" },
        equippedVictoryFx = selectedVictoryEffectId,
        unlockedCosmetics = parsedCosmetics,
        claimedMilestones = parsedMilestones,
        dailyStreakDay = dailyLoginStreak.coerceIn(1, 7),
        lastDailyClaimTimestamp = lastDailyClaimTimestamp,
        weeklyResetTimestamp = weeklyResetTimestamp,
        seasonPassTier = seasonPassTier,
        seasonXp = seasonPassXp,
        isSeasonPassPremium = isSeasonPassPremium,
        hasRemovedAds = hasRemovedAds,
        soundEnabled = soundEnabled,
        musicEnabled = musicEnabled,
        musicVolume = musicVolume.coerceIn(0f, 1f),
        sfxVolume = sfxVolume.coerceIn(0f, 1f),
        vibrationEnabled = vibrationEnabled,
        languageCode = languageCode,
        isTutorialCompleted = isTutorialCompleted
    )
}

fun PlayerProfile.toEntity(): PlayerProfileEntity {
    return PlayerProfileEntity(
        id = 1,
        username = username,
        avatarId = equippedAvatar.ifBlank { avatarId },
        level = level.coerceIn(1, LevelProgression.MAX_LEVEL),
        xp = xp.coerceAtLeast(0),
        trophies = trophies.coerceAtLeast(0),
        coins = coins.coerceAtLeast(0),
        gems = gems.coerceAtLeast(0),
        wins = wins,
        losses = losses,
        winStreak = winStreak,
        highestStreak = highestStreak,
        totalMatches = totalMatches,
        totalCaptures = totalCaptures,
        selectedDiceId = equippedDiceSkin,
        selectedTokenSkinId = equippedTokenSkin,
        selectedBoardThemeId = equippedBoardTheme,
        selectedVictoryEffectId = equippedVictoryFx,
        unlockedCosmeticsCsv = unlockedCosmetics.joinToString(","),
        dailyLoginStreak = dailyStreakDay.coerceIn(1, 7),
        lastDailyClaimTimestamp = lastDailyClaimTimestamp,
        seasonPassTier = seasonPassTier,
        seasonPassXp = seasonXp,
        isSeasonPassPremium = isSeasonPassPremium,
        hasRemovedAds = hasRemovedAds,
        soundEnabled = soundEnabled,
        musicEnabled = musicEnabled,
        vibrationEnabled = vibrationEnabled,
        languageCode = languageCode,
        isTutorialCompleted = isTutorialCompleted,
        selectedProfileFrameId = equippedProfileFrame.ifBlank { "frame_classic" },
        musicVolume = musicVolume.coerceIn(0f, 1f),
        sfxVolume = sfxVolume.coerceIn(0f, 1f),
        claimedMilestonesCsv = claimedMilestones.sorted().joinToString(","),
        tokensFinished = tokensFinished,
        weeklyResetTimestamp = weeklyResetTimestamp
    )
}

fun GameState.toSavedEntity(): SavedGameStateEntity {
    val tokensStr = allTokens.flatMap { (side, tokens) ->
        tokens.map { "${side.name}:${it.id}:${it.state.name}:${it.trackPos}:${it.homeStep}" }
    }.joinToString(";")

    val playersCsv = activePlayers.joinToString(",") { it.name }
    val typesCsv = playerTypes.map { "${it.key.name}:${it.value.name}" }.joinToString(",")

    return SavedGameStateEntity(
        id = 1,
        mode = mode.name,
        difficulty = difficulty.name,
        currentTurn = currentTurn.name,
        diceValue = diceValue,
        hasRolled = hasRolled,
        consecutiveSixes = consecutiveSixes,
        activePlayersCsv = playersCsv,
        playerTypesCsv = typesCsv,
        isPassAndPlay = isPassAndPlay,
        tokensCsv = tokensStr
    )
}

fun SavedGameStateEntity.toDomainGameState(): GameState? {
    try {
        val gameMode = GameMode.valueOf(mode)
        val aiDiff = AIDifficulty.valueOf(difficulty)
        val turn = PlayerSide.valueOf(currentTurn)
        val active = activePlayersCsv.split(",").mapNotNull { runCatching { PlayerSide.valueOf(it) }.getOrNull() }
        val pTypes = playerTypesCsv.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) {
                val side = runCatching { PlayerSide.valueOf(parts[0]) }.getOrNull()
                val type = runCatching { PlayerType.valueOf(parts[1]) }.getOrNull()
                if (side != null && type != null) side to type else null
            } else null
        }.toMap()

        val tokensMap = mutableMapOf<PlayerSide, MutableList<Token>>()
        if (tokensCsv.isNotBlank()) {
            tokensCsv.split(";").forEach { item ->
                val p = item.split(":")
                if (p.size == 5) {
                    val side = runCatching { PlayerSide.valueOf(p[0]) }.getOrNull() ?: return@forEach
                    val id = p[1].toIntOrNull() ?: 0
                    val state = runCatching { TokenState.valueOf(p[2]) }.getOrNull() ?: TokenState.BASE
                    val trackPos = p[3].toIntOrNull() ?: 0
                    val homeStep = p[4].toIntOrNull() ?: 0
                    val tokenList = tokensMap.getOrPut(side) { mutableListOf() }
                    tokenList.add(Token(id = id, side = side, state = state, trackPos = trackPos, homeStep = homeStep))
                }
            }
        }

        return GameState(
            mode = gameMode,
            difficulty = aiDiff,
            matchTimeRemainingSeconds = if (gameMode == GameMode.CLASSIC) 0 else 60,
            turnTimeRemainingSeconds = if (gameMode == GameMode.CLASSIC) 15 else 8,
            currentTurn = turn,
            diceValue = diceValue,
            hasRolled = hasRolled,
            isRolling = false,
            allTokens = tokensMap,
            activePlayers = if (active.isNotEmpty()) active else listOf(PlayerSide.RED, PlayerSide.YELLOW),
            playerTypes = pTypes,
            isPassAndPlay = isPassAndPlay,
            consecutiveSixes = consecutiveSixes
        )
    } catch (e: Exception) {
        return null
    }
}
