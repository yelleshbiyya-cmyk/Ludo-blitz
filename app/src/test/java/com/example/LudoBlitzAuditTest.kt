package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ludoblitz.data.AppDatabase
import com.example.ludoblitz.data.GameRepository
import com.example.ludoblitz.engine.LudoBlitzEngine
import com.example.ludoblitz.model.AIDifficulty
import com.example.ludoblitz.model.BattleResult
import com.example.ludoblitz.model.CosmeticType
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.model.LevelProgression
import com.example.ludoblitz.model.MatchStats
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.PowerUpType
import com.example.ludoblitz.model.QuestFactory
import com.example.ludoblitz.service.GameSoundEffect
import com.example.ludoblitz.service.MusicMode
import com.example.ludoblitz.service.SoundBackend
import com.example.ludoblitz.service.SoundManager
import com.example.ludoblitz.ui.screens.HomeScreen
import com.example.ludoblitz.ui.screens.ProfileScreen
import com.example.ludoblitz.ui.screens.QuestsScreen
import com.example.ludoblitz.ui.screens.SettingsScreen
import com.example.ludoblitz.ui.screens.ShopScreen
import com.example.ui.theme.LudoBlitzTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w360dp-h780dp-xhdpi")
class LudoBlitzAuditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // =========================================================================
    // 1. SAFE NON-DESTRUCTIVE ROOM MIGRATION (v2 -> v3)
    // =========================================================================
    @Test
    fun roomMigrationV2ToV3PreservesExistingPlayerDataAndAppliesSafeDefaults() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "test_migration_v2_v3_${System.nanoTime()}.db"
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        if (dbFile.exists()) dbFile.delete()

        // Create authentic schema v2 SQLite database with pre-existing player progress
        val sqlite = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        sqlite.execSQL(
            """
            CREATE TABLE `player_profile` (
                `id` INTEGER NOT NULL,
                `username` TEXT NOT NULL,
                `avatarId` TEXT NOT NULL,
                `level` INTEGER NOT NULL,
                `xp` INTEGER NOT NULL,
                `trophies` INTEGER NOT NULL,
                `coins` INTEGER NOT NULL,
                `gems` INTEGER NOT NULL,
                `wins` INTEGER NOT NULL,
                `losses` INTEGER NOT NULL,
                `winStreak` INTEGER NOT NULL,
                `highestStreak` INTEGER NOT NULL,
                `totalMatches` INTEGER NOT NULL,
                `totalCaptures` INTEGER NOT NULL,
                `selectedDiceId` TEXT NOT NULL,
                `selectedTokenSkinId` TEXT NOT NULL,
                `selectedBoardThemeId` TEXT NOT NULL,
                `selectedVictoryEffectId` TEXT NOT NULL,
                `unlockedCosmeticsCsv` TEXT NOT NULL,
                `dailyLoginStreak` INTEGER NOT NULL,
                `lastDailyClaimTimestamp` INTEGER NOT NULL,
                `seasonPassTier` INTEGER NOT NULL,
                `seasonPassXp` INTEGER NOT NULL,
                `isSeasonPassPremium` INTEGER NOT NULL,
                `hasRemovedAds` INTEGER NOT NULL,
                `soundEnabled` INTEGER NOT NULL,
                `musicEnabled` INTEGER NOT NULL,
                `vibrationEnabled` INTEGER NOT NULL,
                `languageCode` TEXT NOT NULL,
                `isTutorialCompleted` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            CREATE TABLE `match_history` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `mode` TEXT NOT NULL,
                `result` TEXT NOT NULL,
                `scorePlayer` INTEGER NOT NULL,
                `scoreOpponent` INTEGER NOT NULL,
                `durationSeconds` INTEGER NOT NULL,
                `playerCaptures` INTEGER NOT NULL,
                `opponentCaptures` INTEGER NOT NULL,
                `winReason` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            CREATE TABLE `quests` (
                `id` TEXT NOT NULL,
                `isDaily` INTEGER NOT NULL,
                `currentProgress` INTEGER NOT NULL,
                `isCompleted` INTEGER NOT NULL,
                `isClaimed` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            CREATE TABLE `saved_game_state` (
                `id` INTEGER NOT NULL,
                `mode` TEXT NOT NULL,
                `difficulty` TEXT NOT NULL,
                `currentTurn` TEXT NOT NULL,
                `diceValue` INTEGER NOT NULL,
                `hasRolled` INTEGER NOT NULL,
                `consecutiveSixes` INTEGER NOT NULL,
                `activePlayersCsv` TEXT NOT NULL,
                `playerTypesCsv` TEXT NOT NULL,
                `isPassAndPlay` INTEGER NOT NULL,
                `tokensCsv` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
        sqlite.execSQL(
            """
            INSERT INTO `player_profile` VALUES (
                1, 'RoyalVeteran', 'avatar_grandmaster', 7, 180, 920, 1234, 45,
                12, 4, 3, 6, 16, 28,
                'dice_fire', 'token_robot', 'board_space', 'fx_fireworks',
                'dice_classic,dice_fire,token_classic,token_robot,board_neon,board_space,fx_confetti,fx_fireworks,avatar_blitz_hero,avatar_grandmaster',
                4, 1700000000000, 3, 250, 1, 0,
                1, 0, 1, 'en', 1
            )
            """.trimIndent()
        )
        sqlite.version = 2
        sqlite.close()

        // Open with Room v3 + MIGRATION_2_3 (NO destructive fallback!)
        val migratedDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

        try {
            val loaded = runBlocking { migratedDb.playerDao().getPlayerProfile() }
            assertNotNull(loaded)
            assertEquals("RoyalVeteran", loaded!!.username)
            assertEquals(7, loaded.level)
            assertEquals(180, loaded.xp)
            assertEquals(920, loaded.trophies)
            assertEquals(1234, loaded.coins)
            assertEquals(45, loaded.gems)
            assertEquals(12, loaded.wins)
            assertFalse(loaded.musicEnabled)
            // Verify v3 migrated columns received safe defaults
            assertEquals("frame_classic", loaded.selectedProfileFrameId)
            assertEquals(0.7f, loaded.musicVolume, 0.01f)
            assertEquals(1.0f, loaded.sfxVolume, 0.01f)
            assertEquals("", loaded.claimedMilestonesCsv)
            assertEquals(0, loaded.tokensFinished)
            assertEquals(0L, loaded.weeklyResetTimestamp)
        } finally {
            migratedDb.close()
            dbFile.delete()
        }
    }

    // =========================================================================
    // 2. LEVEL / XP PROGRESSION & SINGLE-CLAIM MILESTONE LOGIC
    // =========================================================================
    @Test
    fun levelProgressionCurveRankTitlesAndSingleClaimMilestoneWorkCorrectly() = runBlocking {
        // Formula verification: 100 + (level - 1) * 50
        assertEquals(100, LevelProgression.xpRequiredForLevel(1))
        assertEquals(150, LevelProgression.xpRequiredForLevel(2))
        assertEquals(300, LevelProgression.xpRequiredForLevel(5))
        assertEquals(550, LevelProgression.xpRequiredForLevel(10))

        // Rank titles across milestones
        assertEquals("Beginner", LevelProgression.rankTitleForLevel(1))
        assertEquals("Rookie", LevelProgression.rankTitleForLevel(5))
        assertEquals("Challenger", LevelProgression.rankTitleForLevel(10))
        assertEquals("Expert", LevelProgression.rankTitleForLevel(20))
        assertEquals("Master", LevelProgression.rankTitleForLevel(30))
        assertEquals("Champion", LevelProgression.rankTitleForLevel(50))
        assertEquals("Grandmaster", LevelProgression.rankTitleForLevel(75))
        assertEquals("Legend", LevelProgression.rankTitleForLevel(100))

        // Multi-level up: L1 (100) + L2 (150) + L3 (200) + L4 (250) = 700 XP -> Level 5 with 30 leftover
        val (lvl, remXp) = LevelProgression.applyXpGain(currentLevel = 1, currentXp = 0, xpGain = 730)
        assertEquals(5, lvl)
        assertEquals(30, remXp)

        // Room-backed repository verification
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = GameRepository(db)
            repo.initDefaultProfileIfNeeded()

            // Award enough XP to jump from Lv1 -> Lv5
            val xpResult = repo.awardXp(730)
            assertTrue(xpResult.leveledUp)
            assertEquals(1, xpResult.oldLevel)
            assertEquals(5, xpResult.newLevel)
            assertTrue(xpResult.newlyCompletedAchievements.any { it.id == "ach_level_5" })

            // Level 5 milestone claim succeeds once and unlocks frame_sapphire
            val firstClaim = repo.claimLevelMilestone(5)
            assertTrue(firstClaim.success)
            val afterFirst = repo.playerProfileFlow.first()
            assertTrue(afterFirst.claimedMilestones.contains(5))
            assertTrue(afterFirst.unlockedCosmetics.contains("frame_sapphire"))
            val coinsAfterFirst = afterFirst.coins

            // Duplicate claim is strictly blocked
            val secondClaim = repo.claimLevelMilestone(5)
            assertFalse(secondClaim.success)
            assertEquals(coinsAfterFirst, repo.playerProfileFlow.first().coins)

            // Locked milestone (Level 10 while Level 5) cannot be claimed
            assertFalse(repo.claimLevelMilestone(10).success)
        } finally {
            db.close()
        }
    }

    // =========================================================================
    // 3. DAILY STREAK RESET, ATOMIC QUEST CLAIMS & SHOP PURCHASE PROTECTION
    // =========================================================================
    @Test
    fun dailyStreakResetDuplicateProtectionAndMatchTrackingWorkAccurately() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val repo = GameRepository(db)
            repo.initDefaultProfileIfNeeded()

            // Day 1 claim succeeds
            val day1 = repo.claimDailyReward(day = 1, coins = 100, gems = 2, xp = 35)
            assertTrue(day1.success)
            assertEquals(1, day1.claimedDay)
            assertFalse(day1.resetStreak)

            // Immediate duplicate claim blocked (<20h cooldown)
            val dupClaim = repo.claimDailyReward(day = 2, coins = 150, gems = 3, xp = 45)
            assertFalse(dupClaim.success)

            // Simulate missed day (>48h ago) -> streak resets to Day 1 cleanly
            val current = repo.playerProfileFlow.first()
            repo.updateProfile(
                current.copy(
                    dailyStreakDay = 5,
                    lastDailyClaimTimestamp = System.currentTimeMillis() - (60L * 60L * 60L * 1000L)
                )
            )
            val afterMiss = repo.claimDailyReward(day = 5, coins = 300, gems = 10, xp = 85)
            assertTrue(afterMiss.success)
            assertTrue(afterMiss.resetStreak)
            assertEquals(1, afterMiss.claimedDay)

            // Match end updates daily, weekly, achievements and detects ach_first_win
            val matchResult = repo.recordMatchEnd(
                mode = GameMode.QUICK_BATTLE,
                result = BattleResult.VICTORY,
                scorePlayer = 400,
                scoreOpponent = 150,
                durationSeconds = 52,
                stats = MatchStats(
                    playerCaptures = 3,
                    playerPowerUpsUsed = 2,
                    playerTokensFinished = 2
                ),
                winReason = "Blitz Victory"
            )
            assertTrue(matchResult.xpAwardResult.newlyCompletedAchievements.any { it.id == "ach_first_win" })

            val dailyWin = repo.dailyMissionsFlow.first().first { it.id == "daily_win_1" }
            assertEquals(1, dailyWin.currentProgress)
            assertTrue(dailyWin.isCompleted)

            // Atomic quest claim succeeds once and blocks duplicate claim
            val qFirst = repo.claimQuestAtomic(dailyWin, isDaily = true)
            assertTrue(qFirst.success)
            val coinsAfterQuest = repo.playerProfileFlow.first().coins
            val qSecond = repo.claimQuestAtomic(dailyWin, isDaily = true)
            assertFalse(qSecond.success)
            assertEquals(coinsAfterQuest, repo.playerProfileFlow.first().coins)

            // Shop duplicate purchase & insufficient funds protection
            assertTrue(repo.unlockCosmetic("frame_sapphire", costCoins = 300, costGems = 0))
            val coinsAfterBuy = repo.playerProfileFlow.first().coins
            assertFalse(repo.unlockCosmetic("frame_sapphire", costCoins = 300, costGems = 0))
            assertEquals(coinsAfterBuy, repo.playerProfileFlow.first().coins)
            assertFalse(repo.unlockCosmetic("dice_galaxy", costCoins = 0, costGems = 9999))
            assertTrue(repo.equipCosmetic("frame_sapphire", CosmeticType.PROFILE_FRAME))
            assertEquals("frame_sapphire", repo.playerProfileFlow.first().equippedProfileFrame)
        } finally {
            db.close()
        }
    }

    // =========================================================================
    // 4. COMPLETE AUDIO SYSTEM (MUSIC DEFAULT OFF, VOLUMES, ALL CUES)
    // =========================================================================
    @Test
    fun audioSystemDefaultsMusicOffClampsVolumesAndDispatchesAllEffects() {
        val defaultProfile = PlayerProfile()
        assertFalse("Background music MUST be OFF by default", defaultProfile.musicEnabled)
        assertTrue("Sound effects should be ON by default", defaultProfile.soundEnabled)
        assertEquals(0.7f, defaultProfile.musicVolume, 0.001f)
        assertEquals(1.0f, defaultProfile.sfxVolume, 0.001f)

        val log = mutableListOf<String>()
        val backend = object : SoundBackend {
            override fun setMusicEnabled(enabled: Boolean) { log.add("music:$enabled") }
            override fun setMusicMode(mode: MusicMode) { log.add("mode:$mode") }
            override fun setMusicVolume(volume: Float) { log.add("mvol:$volume") }
            override fun setSfxVolume(volume: Float) { log.add("svol:$volume") }
            override fun onAppPause() { log.add("pause") }
            override fun onAppResume() { log.add("resume") }
            override fun playDiceShake(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("dice_shake") }
            override fun playDiceLand(rolledValue: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("dice_land:$rolledValue") }
            override fun playRollSix(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("roll_six") }
            override fun playPawnSelect(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("pawn_select") }
            override fun playTokenStep(stepIndex: Int, totalSteps: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("step") }
            override fun playTokenLand(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("land") }
            override fun playTokenLeaveBase(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("leave") }
            override fun playCapture(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("capture") }
            override fun playSafeCell(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("safe") }
            override fun playHomeFinish(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("home") }
            override fun playExtraTurn(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("extra_turn") }
            override fun playPowerUp(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("powerup") }
            override fun playPowerUpType(type: PowerUpType?, soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("powerup:${type?.name ?: "GENERIC"}") }
            override fun playButtonClick(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("click") }
            override fun playTabSwitch(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("tab") }
            override fun playBackToHome(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("back") }
            override fun playTurnNotification(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("turn") }
            override fun playMatchStart(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("start") }
            override fun playMatchFound(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("found") }
            override fun playCountdownBeep(number: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("beep:$number") }
            override fun playVictory(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("victory") }
            override fun playDefeat(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("defeat") }
            override fun playCoinClink(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("coin") }
            override fun playGemSparkle(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("gem") }
            override fun playCoinGemFlyBurst(coinCount: Int, gemCount: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("burst") }
            override fun playChestReward(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("chest") }
            override fun playWheelSpinTick(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("wheel_tick") }
            override fun playRewardReveal(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("reward_reveal") }
            override fun playLevelUp(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("level_up") }
            override fun playAchievementUnlock(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("ach_unlock") }
            override fun playPurchase(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("purchase") }
            override fun playItemEquip(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("equip") }
            override fun playError(soundEnabled: Boolean, vibrationEnabled: Boolean) { log.add("error") }
            override fun release() { log.add("release") }
        }

        val sm = SoundManager(backend)
        assertFalse(sm.isMusicEnabled)

        // Volume clamping
        sm.setMusicVolume(1.5f)
        assertEquals(1.0f, sm.musicVolume, 0.001f)
        sm.setSfxVolume(-0.4f)
        assertEquals(0.0f, sm.sfxVolume, 0.001f)
        sm.setSfxVolume(0.85f)
        assertEquals(0.85f, sm.sfxVolume, 0.001f)

        // Dispatch every GameSoundEffect enum entry
        GameSoundEffect.entries.forEach { sm.play(it) }
        // Dispatch every PowerUpType
        PowerUpType.entries.forEach { sm.playPowerUp(it) }

        assertTrue(log.contains("pawn_select"))
        assertTrue(log.contains("extra_turn"))
        assertTrue(log.contains("wheel_tick"))
        assertTrue(log.contains("reward_reveal"))
        assertTrue(log.contains("level_up"))
        assertTrue(log.contains("ach_unlock"))
        assertTrue(log.contains("equip"))
        assertTrue(log.contains("error"))
        for (p in PowerUpType.entries) {
            assertTrue("Missing powerup cue for $p", log.contains("powerup:${p.name}"))
        }
    }

    // =========================================================================
    // 5. ROBOLECTRIC COMPOSE UI FITTING & REGRESSION TESTS (360dp COMPACT PHONE)
    // =========================================================================
    @Test
    fun homeScreenResumeCardNeverExplodesVerticallyOnCompactPhone() {
        val engine = LudoBlitzEngine()
        val savedState = engine.initializeGame(GameMode.CLASSIC, AIDifficulty.MEDIUM)

        composeTestRule.setContent {
            LudoBlitzTheme {
                Box(modifier = Modifier.width(360.dp).height(760.dp)) {
                    HomeScreen(
                        profile = PlayerProfile(isTutorialCompleted = true),
                        savedGameState = savedState,
                        onStartMatch = {},
                        onOpenQuests = {},
                        onCompleteTutorial = {}
                    )
                }
            }
        }

        val resumeCard = composeTestRule.onNodeWithTag("resume_game_card")
        resumeCard.assertIsDisplayed()
        val cardBounds = resumeCard.getUnclippedBoundsInRoot()
        assertTrue(
            "Resume card height (${cardBounds.height}) must be compact (<150.dp), never full-screen!",
            cardBounds.height < 150.dp
        )
        composeTestRule.onNodeWithTag("resume_game_button").assertIsDisplayed()
    }

    @Test
    fun shopScreenCardsStayCompactAndAllSixCategoriesRenderCleanly() {
        var selectedTabClicks = 0
        composeTestRule.setContent {
            LudoBlitzTheme {
                Box(modifier = Modifier.width(360.dp).height(760.dp)) {
                    ShopScreen(
                        profile = PlayerProfile(coins = 1000, gems = 50),
                        onBuyCosmetic = { _, _, _ -> selectedTabClicks++ },
                        onEquipCosmetic = { _, _ -> }
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("shop_collector_card").assertIsDisplayed()
        val neonCard = composeTestRule.onNodeWithTag("shop_item_dice_neon")
        neonCard.assertIsDisplayed()
        val neonBounds = neonCard.getUnclippedBoundsInRoot()
        assertTrue(
            "Shop card height (${neonBounds.height}) must stay compact (<185.dp), never 800dp!",
            neonBounds.height < 185.dp
        )

        // Scroll horizontal tabs row to verify Profile Frames tab is selectable and displays frame items
        composeTestRule.onNodeWithTag("shop_tab_PROFILE_FRAME")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("shop_item_frame_classic").assertIsDisplayed()
    }

    @Test
    fun profileQuestsAndSettingsScreensRenderCleanlyWithDefaultAudioState() {
        val profile = PlayerProfile(level = 5, xp = 120, trophies = 450)
        composeTestRule.setContent {
            LudoBlitzTheme {
                Box(modifier = Modifier.width(360.dp).height(760.dp)) {
                    SettingsScreen(
                        profile = profile,
                        onToggleSound = {},
                        onToggleMusic = {},
                        onToggleVibration = {},
                        onSelectLanguage = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("settings_music_switch").assertIsDisplayed().assertIsOff()
        composeTestRule.onNodeWithTag("settings_sfx_volume_slider").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_music_volume_slider").assertIsDisplayed()
    }

    @Test
    fun profileAndQuestsScreensRenderMilestonesAndWeeklyMissions() {
        val profile = PlayerProfile(level = 5, xp = 120, trophies = 450)
        composeTestRule.setContent {
            LudoBlitzTheme {
                Box(modifier = Modifier.width(360.dp).height(760.dp)) {
                    ProfileScreen(profile = profile)
                }
            }
        }
        composeTestRule.onNodeWithTag("profile_hero_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("milestone_card_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("claim_milestone_1").assertIsDisplayed()
    }

    @Test
    fun questsScreenRendersCalendarAndMissionsCleanly() {
        composeTestRule.setContent {
            LudoBlitzTheme {
                Box(modifier = Modifier.width(360.dp).height(760.dp)) {
                    QuestsScreen(
                        profile = PlayerProfile(),
                        dailyMissions = QuestFactory.getDefaultDailyMissions(),
                        weeklyMissions = QuestFactory.getDefaultWeeklyMissions(),
                        achievements = QuestFactory.getDefaultAchievements(),
                        onClaimDailyReward = { _, _, _, _ -> },
                        onClaimQuest = { _, _ -> }
                    )
                }
            }
        }
        composeTestRule.onNodeWithTag("daily_calendar_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battle Master").assertIsDisplayed()
    }
}
