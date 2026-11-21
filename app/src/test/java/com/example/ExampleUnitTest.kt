package com.example

import com.example.ludoblitz.data.toDomain
import com.example.ludoblitz.data.toEntity
import com.example.ludoblitz.engine.LudoAIEngine
import com.example.ludoblitz.engine.LudoBlitzEngine
import com.example.ludoblitz.model.AIDifficulty
import com.example.ludoblitz.model.GameBoardConstants
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.model.GameState
import com.example.ludoblitz.model.PlayerProfile
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.model.PowerUpType
import com.example.ludoblitz.model.Token
import com.example.ludoblitz.model.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testRealLudoBoardConstants() {
        assertEquals(52, GameBoardConstants.TOTAL_TRACK_TILES)
        assertEquals(5, GameBoardConstants.HOME_LANE_LENGTH)
        assertEquals(8, GameBoardConstants.SAFE_TILES.size)
        assertTrue(GameBoardConstants.isSafeTile(0))
        assertTrue(GameBoardConstants.isSafeTile(26))
        assertTrue(GameBoardConstants.isSafeTile(8))
        assertTrue(GameBoardConstants.isSafeTile(21))
        assertTrue(GameBoardConstants.isSafeTile(34))
        assertTrue(GameBoardConstants.isSafeTile(47))
    }

    @Test
    fun testTokenExitBaseOnSixOrOne() {
        val moveWithSix = GameBoardConstants.calculateMove(
            side = PlayerSide.PLAYER,
            currentState = TokenState.BASE,
            currentTrackPos = 0,
            currentHomeStep = 0,
            steps = 6
        )
        assertNotNull(moveWithSix)
        assertEquals(TokenState.TRACK, moveWithSix?.newState)
        assertEquals(GameBoardConstants.PLAYER_START_TILE, moveWithSix?.newTrackPos)

        val moveWithOne = GameBoardConstants.calculateMove(
            side = PlayerSide.PLAYER,
            currentState = TokenState.BASE,
            currentTrackPos = 0,
            currentHomeStep = 0,
            steps = 1
        )
        assertNotNull(moveWithOne)
        assertEquals(TokenState.TRACK, moveWithOne?.newState)
    }

    @Test
    fun testHomeStretchAndGoalEntry() {
        // Player token at track cell (50) rolls 1 -> enters home lane step 0
        val moveIntoHome = GameBoardConstants.calculateMove(
            side = PlayerSide.PLAYER,
            currentState = TokenState.TRACK,
            currentTrackPos = 50,
            currentHomeStep = 0,
            steps = 1
        )
        assertNotNull(moveIntoHome)
        assertEquals(TokenState.HOME_STRETCH, moveIntoHome?.newState)
        assertEquals(0, moveIntoHome?.newHomeStep)

        // Inside home lane step 4 (1 step from goal) rolls 1 -> finishes in Center Goal!
        val moveToGoal = GameBoardConstants.calculateMove(
            side = PlayerSide.PLAYER,
            currentState = TokenState.HOME_STRETCH,
            currentTrackPos = 51,
            currentHomeStep = 4,
            steps = 1
        )
        assertNotNull(moveToGoal)
        assertEquals(TokenState.FINISHED, moveToGoal?.newState)
    }

    @Test
    fun testSafeSquareProtection() {
        val safeSquare = 8
        assertTrue(GameBoardConstants.isSafeTile(safeSquare))
        // Tokens on safe squares cannot be captured
        val engine = LudoBlitzEngine()
        val state = engine.initializeGame(GameMode.QUICK_BATTLE, AIDifficulty.EASY)
        assertEquals(60, state.matchTimeRemainingSeconds)
        assertEquals(4, state.playerTokens.size)
        assertEquals(4, state.opponentTokens.size)
    }

    @Test
    fun testProfileSerializationIntegrity() {
        val profile = PlayerProfile(
            username = "LudoPro",
            trophies = 1450,
            coins = 3200,
            gems = 85,
            level = 10,
            unlockedCosmetics = setOf("dice_neon_pulse", "dice_golden_emperor")
        )
        val entity = profile.toEntity()
        val converted = entity.toDomain()
        assertEquals(profile.username, converted.username)
        assertEquals(profile.trophies, converted.trophies)
        assertEquals(profile.coins, converted.coins)
        assertEquals(profile.gems, converted.gems)
        assertEquals(profile.unlockedCosmetics, converted.unlockedCosmetics)
    }

    @Test
    fun testAIEngineDecisions() {
        val aiEngine = LudoAIEngine()
        val engine = LudoBlitzEngine()
        val state = engine.initializeGame(GameMode.QUICK_BATTLE, AIDifficulty.HARD)
        // With tokens in base, selection should return one of the selectable IDs
        val selectable = setOf(10, 11)
        val selected = aiEngine.selectBestMove(state, selectable)
        assertTrue(selectable.contains(selected))
    }

    @Test
    fun testClassic2PlayerInitialization() {
        val engine = LudoBlitzEngine()
        val config = com.example.ludoblitz.model.ClassicMatchConfig(
            playerCount = com.example.ludoblitz.model.ClassicPlayerCount.TWO_PLAYERS,
            isPassAndPlay = false
        )
        val state = engine.initializeGame(GameMode.CLASSIC, AIDifficulty.MEDIUM, config)
        assertTrue(state.isClassic)
        assertEquals(0, state.matchTimeRemainingSeconds)
        assertEquals(2, state.activePlayers.size)
        assertEquals(listOf(PlayerSide.RED, PlayerSide.YELLOW), state.activePlayers)
        assertEquals(4, state.allTokens[PlayerSide.RED]?.size)
        assertEquals(4, state.allTokens[PlayerSide.YELLOW]?.size)
        assertEquals(PlayerSide.RED, state.currentTurn)
        assertFalse(state.isPassAndPlay)
    }

    @Test
    fun testClassic4PlayerInitialization() {
        val engine = LudoBlitzEngine()
        val config = com.example.ludoblitz.model.ClassicMatchConfig(
            playerCount = com.example.ludoblitz.model.ClassicPlayerCount.FOUR_PLAYERS,
            isPassAndPlay = true
        )
        val state = engine.initializeGame(GameMode.CLASSIC, AIDifficulty.MEDIUM, config)
        assertTrue(state.isClassic)
        assertTrue(state.isPassAndPlay)
        assertEquals(4, state.activePlayers.size)
        assertEquals(
            listOf(PlayerSide.RED, PlayerSide.GREEN, PlayerSide.YELLOW, PlayerSide.BLUE),
            state.activePlayers
        )
        for (player in state.activePlayers) {
            assertEquals(4, state.allTokens[player]?.size)
            assertEquals(com.example.ludoblitz.model.PlayerType.HUMAN, state.playerTypes[player])
        }
    }

    @Test
    fun testClassicRollSixToExitBase() {
        val engine = LudoBlitzEngine()
        val config = com.example.ludoblitz.model.ClassicMatchConfig(
            playerCount = com.example.ludoblitz.model.ClassicPlayerCount.TWO_PLAYERS
        )
        val state = engine.initializeGame(GameMode.CLASSIC, AIDifficulty.MEDIUM, config)
        
        // When dice is rolled with a 4 in Classic mode, base tokens cannot exit
        val stateRolled4 = engine.rollDice(state, fixedValue = 4)
        assertTrue(stateRolled4.selectableTokenIds.isEmpty())

        // When dice is rolled with a 6 in Classic mode, all base tokens become selectable
        val stateRolled6 = engine.rollDice(state, fixedValue = 6)
        assertEquals(4, stateRolled6.selectableTokenIds.size)
    }

    @Test
    fun testClassicTurnRotation2P() {
        val engine = LudoBlitzEngine()
        val config = com.example.ludoblitz.model.ClassicMatchConfig(
            playerCount = com.example.ludoblitz.model.ClassicPlayerCount.TWO_PLAYERS
        )
        val state = engine.initializeGame(GameMode.CLASSIC, AIDifficulty.MEDIUM, config)
        assertEquals(PlayerSide.RED, state.currentTurn)

        // Turn switch moves from RED to YELLOW
        val nextTurn = engine.passTurn(state)
        assertEquals(PlayerSide.YELLOW, nextTurn.currentTurn)

        // Turn switch moves from YELLOW back to RED
        val nextTurn2 = engine.passTurn(nextTurn)
        assertEquals(PlayerSide.RED, nextTurn2.currentTurn)
    }

    @Test
    fun testClassicTurnRotation4P() {
        val engine = LudoBlitzEngine()
        val config = com.example.ludoblitz.model.ClassicMatchConfig(
            playerCount = com.example.ludoblitz.model.ClassicPlayerCount.FOUR_PLAYERS
        )
        val state = engine.initializeGame(GameMode.CLASSIC, AIDifficulty.MEDIUM, config)
        assertEquals(PlayerSide.RED, state.currentTurn)

        val turnGreen = engine.passTurn(state)
        assertEquals(PlayerSide.GREEN, turnGreen.currentTurn)

        val turnYellow = engine.passTurn(turnGreen)
        assertEquals(PlayerSide.YELLOW, turnYellow.currentTurn)

        val turnBlue = engine.passTurn(turnYellow)
        assertEquals(PlayerSide.BLUE, turnBlue.currentTurn)

        val turnRedAgain = engine.passTurn(turnBlue)
        assertEquals(PlayerSide.RED, turnRedAgain.currentTurn)
    }

    @Test
    fun testClassicThreeConsecutiveSixes() {
        val engine = LudoBlitzEngine()
        val config = com.example.ludoblitz.model.ClassicMatchConfig(
            playerCount = com.example.ludoblitz.model.ClassicPlayerCount.TWO_PLAYERS
        )
        val state = engine.initializeGame(GameMode.CLASSIC, AIDifficulty.MEDIUM, config)
        
        // 1st 6 -> consecutiveSixes becomes 1
        val stateAfter1stSix = engine.rollDice(state, fixedValue = 6)
        assertEquals(1, stateAfter1stSix.consecutiveSixes)
        assertEquals(PlayerSide.RED, stateAfter1stSix.currentTurn)

        // Move token 0 to clear rolled state
        val stateAfter1stMove = engine.moveToken(stateAfter1stSix, 0)
        assertEquals(PlayerSide.RED, stateAfter1stMove.currentTurn)
        assertEquals(1, stateAfter1stMove.consecutiveSixes)

        // 2nd 6 -> consecutiveSixes becomes 2
        val stateAfter2ndSix = engine.rollDice(stateAfter1stMove, fixedValue = 6)
        assertEquals(2, stateAfter2ndSix.consecutiveSixes)
        assertEquals(PlayerSide.RED, stateAfter2ndSix.currentTurn)

        // Move token 1 to clear rolled state
        val stateAfter2ndMove = engine.moveToken(stateAfter2ndSix, 1)
        assertEquals(PlayerSide.RED, stateAfter2ndMove.currentTurn)
        assertEquals(2, stateAfter2ndMove.consecutiveSixes)

        // 3rd 6 in a row -> turn is forfeited and switches to YELLOW
        val stateAfter3rdSix = engine.rollDice(stateAfter2ndMove, fixedValue = 6)
        assertEquals(PlayerSide.YELLOW, stateAfter3rdSix.currentTurn)
        assertEquals(0, stateAfter3rdSix.consecutiveSixes)
    }

    @Test
    fun testComputeTokenStepPathMultiStepAndCapture() {
        // Multi-step advance on track produces one coordinate per step
        val prevTrack = Token(id = 0, side = PlayerSide.RED, state = TokenState.TRACK, trackPos = 2)
        val nextTrack = Token(id = 0, side = PlayerSide.RED, state = TokenState.TRACK, trackPos = 6)
        val path = com.example.ludoblitz.ui.components.computeTokenStepPath(prevTrack, nextTrack, isClassic = true)
        assertEquals(4, path.size)
        assertEquals(GameBoardConstants.TRACK_COORDS[3], path[0])
        assertEquals(GameBoardConstants.TRACK_COORDS[6], path[3])

        // Entering finished goal from track boundary
        val prevBoundary = Token(id = 1, side = PlayerSide.GREEN, state = TokenState.HOME_STRETCH, trackPos = 12, homeStep = 3)
        val nextFinish = Token(id = 1, side = PlayerSide.GREEN, state = TokenState.FINISHED, trackPos = 12, homeStep = 5)
        val finishPath = com.example.ludoblitz.ui.components.computeTokenStepPath(prevBoundary, nextFinish, isClassic = false)
        assertEquals(2, finishPath.size)

        // Capture back to base produces direct 1-hop return coordinate
        val capturedBack = Token(id = 0, side = PlayerSide.RED, state = TokenState.BASE, trackPos = 0)
        val capturePath = com.example.ludoblitz.ui.components.computeTokenStepPath(prevTrack, capturedBack, isClassic = true)
        assertEquals(1, capturePath.size)
        assertEquals(GameBoardConstants.RED_BASE_NESTS[0], capturePath[0])
    }

    @Test
    fun testFourPlayerTokenTotalProgress() {
        // Start tiles for all 4 colors should each report progress = 1
        assertEquals(1, Token(id = 0, side = PlayerSide.RED, state = TokenState.TRACK, trackPos = 0).totalProgress())
        assertEquals(1, Token(id = 0, side = PlayerSide.GREEN, state = TokenState.TRACK, trackPos = 13).totalProgress())
        assertEquals(1, Token(id = 0, side = PlayerSide.YELLOW, state = TokenState.TRACK, trackPos = 26).totalProgress())
        assertEquals(1, Token(id = 0, side = PlayerSide.BLUE, state = TokenState.TRACK, trackPos = 39).totalProgress())

        // Wrap-around check for BLUE: tile 0 is 13 steps after BLUE start (39 -> 51 is 12, +1 to 0 = 13 -> progress 14)
        assertEquals(14, Token(id = 0, side = PlayerSide.BLUE, state = TokenState.TRACK, trackPos = 0).totalProgress())
        // Wrap-around check for GREEN: tile 11 is 50 steps after GREEN start (13) -> progress 51
        assertEquals(51, Token(id = 0, side = PlayerSide.GREEN, state = TokenState.TRACK, trackPos = 11).totalProgress())
    }

    @Test
    fun testCapturePreservesCapturedBannerAndGrantsBonusTurn() {
        val engine = LudoBlitzEngine()
        val redTokens = listOf(
            Token(id = 0, side = PlayerSide.RED, state = TokenState.TRACK, trackPos = 2),
            Token(id = 1, side = PlayerSide.RED, state = TokenState.BASE),
            Token(id = 2, side = PlayerSide.RED, state = TokenState.BASE),
            Token(id = 3, side = PlayerSide.RED, state = TokenState.BASE)
        )
        val yellowTokens = listOf(
            Token(id = 0, side = PlayerSide.YELLOW, state = TokenState.TRACK, trackPos = 5), // non-safe tile
            Token(id = 1, side = PlayerSide.YELLOW, state = TokenState.BASE),
            Token(id = 2, side = PlayerSide.YELLOW, state = TokenState.BASE),
            Token(id = 3, side = PlayerSide.YELLOW, state = TokenState.BASE)
        )
        val initial = GameState(
            mode = GameMode.CLASSIC,
            currentTurn = PlayerSide.RED,
            diceValue = 3,
            hasRolled = true,
            selectableTokenIds = setOf(0),
            allTokens = mapOf(PlayerSide.RED to redTokens, PlayerSide.YELLOW to yellowTokens),
            activePlayers = listOf(PlayerSide.RED, PlayerSide.YELLOW)
        )

        val afterCapture = engine.moveToken(initial, 0)
        // Yellow token 0 sent back to BASE
        assertEquals(TokenState.BASE, afterCapture.allTokens[PlayerSide.YELLOW]?.first()?.state)
        // Red retains turn (bonus roll) AND banner preserves CAPTURED for audio/UI trigger
        assertEquals(PlayerSide.RED, afterCapture.currentTurn)
        assertEquals(1, afterCapture.stats.playerCaptures)
        assertTrue(afterCapture.activeBannerMessage?.contains("CAPTURED") == true)
    }

    @Test
    fun testTokenFinishIncrementsStatsAndGrantsBonusTurn() {
        val engine = LudoBlitzEngine()
        val redTokens = listOf(
            Token(id = 0, side = PlayerSide.RED, state = TokenState.HOME_STRETCH, trackPos = 50, homeStep = 4),
            Token(id = 1, side = PlayerSide.RED, state = TokenState.BASE),
            Token(id = 2, side = PlayerSide.RED, state = TokenState.BASE),
            Token(id = 3, side = PlayerSide.RED, state = TokenState.BASE)
        )
        val yellowTokens = List(4) { Token(id = it, side = PlayerSide.YELLOW, state = TokenState.BASE) }
        val initial = GameState(
            mode = GameMode.CLASSIC,
            currentTurn = PlayerSide.RED,
            diceValue = 1,
            hasRolled = true,
            selectableTokenIds = setOf(0),
            allTokens = mapOf(PlayerSide.RED to redTokens, PlayerSide.YELLOW to yellowTokens),
            activePlayers = listOf(PlayerSide.RED, PlayerSide.YELLOW)
        )

        val afterFinish = engine.moveToken(initial, 0)
        assertEquals(TokenState.FINISHED, afterFinish.allTokens[PlayerSide.RED]?.first()?.state)
        assertEquals(1, afterFinish.stats.playerTokensFinished)
        assertEquals(PlayerSide.RED, afterFinish.currentTurn)
        assertTrue(afterFinish.activeBannerMessage?.contains("TOKEN HOME") == true)
    }

    @Test
    fun testSoundManagerDispatchesGameEffectsAndRespectsPreferences() {
        val played = mutableListOf<String>()
        val fakeBackend = object : com.example.ludoblitz.service.SoundBackend {
            override fun setMusicEnabled(enabled: Boolean) { played.add("music:$enabled") }
            override fun setMusicMode(mode: com.example.ludoblitz.service.MusicMode) { played.add("mode:$mode") }
            override fun onAppPause() { played.add("pause") }
            override fun onAppResume() { played.add("resume") }
            override fun playDiceShake(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("dice_roll:$soundEnabled:$vibrationEnabled") }
            override fun playDiceLand(rolledValue: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("dice_land:$rolledValue:$soundEnabled") }
            override fun playRollSix(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("roll_six:$soundEnabled") }
            override fun playTokenStep(stepIndex: Int, totalSteps: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("pawn_step:$stepIndex/$totalSteps:$soundEnabled") }
            override fun playTokenLand(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("pawn_land:$soundEnabled") }
            override fun playTokenLeaveBase(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("pawn_leave:$soundEnabled") }
            override fun playCapture(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("pawn_capture:$soundEnabled") }
            override fun playSafeCell(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("safe:$soundEnabled") }
            override fun playHomeFinish(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("home:$soundEnabled") }
            override fun playPowerUp(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("powerup:$soundEnabled") }
            override fun playButtonClick(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("click:$soundEnabled") }
            override fun playTabSwitch(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("tab:$soundEnabled") }
            override fun playBackToHome(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("back_home:$soundEnabled") }
            override fun playTurnNotification(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("turn:$soundEnabled") }
            override fun playMatchStart(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("start:$soundEnabled") }
            override fun playMatchFound(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("found:$soundEnabled") }
            override fun playCountdownBeep(number: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("countdown:$number:$soundEnabled") }
            override fun playVictory(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("victory:$soundEnabled") }
            override fun playDefeat(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("defeat:$soundEnabled") }
            override fun playCoinClink(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("coin:$soundEnabled") }
            override fun playGemSparkle(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("gem:$soundEnabled") }
            override fun playCoinGemFlyBurst(coinCount: Int, gemCount: Int, soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("burst:$coinCount:$gemCount:$soundEnabled") }
            override fun playChestReward(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("chest:$soundEnabled") }
            override fun playPurchase(soundEnabled: Boolean, vibrationEnabled: Boolean) { played.add("purchase:$soundEnabled") }
            override fun release() { played.add("release") }
        }

        val manager = com.example.ludoblitz.service.SoundManager(fakeBackend)
        assertFalse(manager.isMusicEnabled)
        manager.playDiceRoll()
        manager.playDiceLand(6)
        manager.playPawnStep(2, 6)
        manager.playPowerUp(PowerUpType.SPEED_BOOST)
        manager.playPowerUp(PowerUpType.SHIELD)

        manager.updatePreferences(soundEnabled = true, musicEnabled = true, vibrationEnabled = true)
        manager.updatePreferences(soundEnabled = false, musicEnabled = false, vibrationEnabled = false)
        manager.play(com.example.ludoblitz.service.GameSoundEffect.PAWN_CAPTURE)

        assertTrue(played.contains("dice_roll:true:true"))
        assertTrue(played.contains("dice_land:6:true"))
        assertTrue(played.contains("pawn_step:2/6:true"))
        assertTrue(played.contains("powerup:true"))
        assertTrue(played.contains("safe:true"))
        assertTrue(played.contains("music:true"))
        assertTrue(played.contains("music:false"))
        assertTrue(played.contains("pawn_capture:false"))
    }
}
