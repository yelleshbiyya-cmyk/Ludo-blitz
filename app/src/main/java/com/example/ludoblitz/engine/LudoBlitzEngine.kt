package com.example.ludoblitz.engine

import com.example.ludoblitz.model.AIDifficulty
import com.example.ludoblitz.model.BattleResult
import com.example.ludoblitz.model.ClassicMatchConfig
import com.example.ludoblitz.model.ClassicPlayerCount
import com.example.ludoblitz.model.GameBoardConstants
import com.example.ludoblitz.model.GameMode
import com.example.ludoblitz.model.GameState
import com.example.ludoblitz.model.MatchStats
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.model.PlayerType
import com.example.ludoblitz.model.PowerUpType
import com.example.ludoblitz.model.Token
import com.example.ludoblitz.model.TokenState
import kotlin.random.Random

class LudoBlitzEngine(
    private val random: Random = Random.Default
) {
    fun initializeGame(
        mode: GameMode,
        difficulty: AIDifficulty,
        classicConfig: ClassicMatchConfig? = null
    ): GameState {
        if (mode == GameMode.CLASSIC) {
            val config = classicConfig ?: ClassicMatchConfig()
            val activeSides = if (config.playerCount == ClassicPlayerCount.FOUR_PLAYERS) {
                listOf(PlayerSide.RED, PlayerSide.GREEN, PlayerSide.YELLOW, PlayerSide.BLUE)
            } else {
                listOf(PlayerSide.RED, PlayerSide.YELLOW)
            }

            val allTokens = activeSides.associateWith { side ->
                List(4) { Token(id = it, side = side, state = TokenState.BASE) }
            }

            val playerTypesMap = activeSides.associateWith { side ->
                if (config.isPassAndPlay) {
                    PlayerType.HUMAN
                } else {
                    config.playerTypes[side] ?: if (side == PlayerSide.RED) PlayerType.HUMAN else PlayerType.AI
                }
            }

            return GameState(
                mode = mode,
                difficulty = difficulty,
                matchTimeRemainingSeconds = 0, // No 60s timer in Classic
                turnTimeRemainingSeconds = 15,
                currentTurn = PlayerSide.RED,
                diceValue = 1,
                hasRolled = false,
                isRolling = false,
                allTokens = allTokens,
                activePlayers = activeSides,
                playerTypes = playerTypesMap,
                isPassAndPlay = config.isPassAndPlay,
                consecutiveSixes = 0,
                selectableTokenIds = emptySet()
            )
        }

        // In Blitz mode, players start with 1 token ready on start tile for instant fast action!
        val playerTokens = listOf(
            Token(id = 0, side = PlayerSide.PLAYER, state = TokenState.TRACK, trackPos = GameBoardConstants.PLAYER_START_TILE),
            Token(id = 1, side = PlayerSide.PLAYER, state = TokenState.BASE),
            Token(id = 2, side = PlayerSide.PLAYER, state = TokenState.BASE),
            Token(id = 3, side = PlayerSide.PLAYER, state = TokenState.BASE)
        )

        val opponentTokens = listOf(
            Token(id = 0, side = PlayerSide.OPPONENT, state = TokenState.TRACK, trackPos = GameBoardConstants.OPPONENT_START_TILE),
            Token(id = 1, side = PlayerSide.OPPONENT, state = TokenState.BASE),
            Token(id = 2, side = PlayerSide.OPPONENT, state = TokenState.BASE),
            Token(id = 3, side = PlayerSide.OPPONENT, state = TokenState.BASE)
        )

        val allTokens = mapOf(
            PlayerSide.PLAYER to playerTokens,
            PlayerSide.OPPONENT to opponentTokens
        )

        return GameState(
            mode = mode,
            difficulty = difficulty,
            matchTimeRemainingSeconds = 60,
            turnTimeRemainingSeconds = 8,
            currentTurn = PlayerSide.PLAYER,
            diceValue = 1,
            hasRolled = false,
            isRolling = false,
            allTokens = allTokens,
            activePlayers = listOf(PlayerSide.PLAYER, PlayerSide.OPPONENT),
            playerTypes = mapOf(PlayerSide.PLAYER to PlayerType.HUMAN, PlayerSide.OPPONENT to PlayerType.AI),
            selectableTokenIds = emptySet()
        )
    }

    /**
     * Executes roll for the current active player
     */
    fun rollDice(state: GameState, fixedValue: Int? = null): GameState {
        if (state.hasRolled || state.isGameOver) return state

        val isClassic = state.isClassic
        val roll1 = fixedValue ?: (random.nextInt(6) + 1)
        val roll2 = if (!isClassic && state.isDoubleDiceActive) (random.nextInt(6) + 1) else 0
        val effectiveSteps = roll1 + roll2

        // Check 3 consecutive 6s rule in Classic
        if (isClassic && roll1 == 6) {
            if (state.consecutiveSixes >= 2) {
                // 3rd consecutive 6 forfeited!
                val nextTurn = getNextPlayer(state.currentTurn, state.activePlayers)
                return state.copy(
                    diceValue = 6,
                    hasRolled = false,
                    isRolling = false,
                    consecutiveSixes = 0,
                    selectableTokenIds = emptySet(),
                    selectedTokenId = null,
                    currentTurn = nextTurn,
                    activeBannerMessage = "⚠️ 3 CONSECUTIVE 6s! TURN FORFEITED!"
                )
            }
        }

        val nextConsecutiveSixes = if (isClassic) {
            if (roll1 == 6) state.consecutiveSixes + 1 else 0
        } else 0

        val currentTokens = state.getTokens(state.currentTurn)
        val legalTokenIds = findLegalMoveTokens(currentTokens, state.currentTurn, effectiveSteps, isClassic)

        val banner = if (!isClassic && state.isDoubleDiceActive) {
            "⚡ DOUBLE DICE: Rolled $roll1 + $roll2 = $effectiveSteps!"
        } else null

        if (legalTokenIds.isEmpty()) {
            // No legal moves available
            return state.copy(
                diceValue = effectiveSteps,
                secondDiceValue = roll2,
                hasRolled = true,
                isRolling = false,
                isDoubleDiceActive = false,
                consecutiveSixes = nextConsecutiveSixes,
                selectableTokenIds = emptySet(),
                activeBannerMessage = banner ?: "No legal moves possible!"
            )
        }

        // If only 1 token can move, auto-select it
        val autoSelected = if (legalTokenIds.size == 1) legalTokenIds.first() else null

        return state.copy(
            diceValue = effectiveSteps,
            secondDiceValue = roll2,
            hasRolled = true,
            isRolling = false,
            isDoubleDiceActive = false,
            consecutiveSixes = nextConsecutiveSixes,
            selectableTokenIds = legalTokenIds,
            selectedTokenId = autoSelected,
            activeBannerMessage = banner
        )
    }

    fun findLegalMoveTokens(tokens: List<Token>, side: PlayerSide, steps: Int, isClassic: Boolean = false): Set<Int> {
        val valid = mutableSetOf<Int>()
        for (token in tokens) {
            val result = GameBoardConstants.calculateMove(
                side = side,
                currentState = token.state,
                currentTrackPos = token.trackPos,
                currentHomeStep = token.homeStep,
                steps = steps,
                isClassic = isClassic
            )
            if (result != null) {
                valid.add(token.id)
            }
        }
        return valid
    }

    /**
     * Moves the specified token and resolves captures and victory conditions
     */
    fun moveToken(state: GameState, tokenId: Int): GameState {
        if (!state.hasRolled || state.isGameOver) return state

        val isClassic = state.isClassic
        val currentSide = state.currentTurn
        val currentSideTokens = state.getTokens(currentSide).toMutableList()

        val tokenIndex = currentSideTokens.indexOfFirst { it.id == tokenId }
        if (tokenIndex == -1) return state

        val token = currentSideTokens[tokenIndex]
        val moveRes = GameBoardConstants.calculateMove(
            side = currentSide,
            currentState = token.state,
            currentTrackPos = token.trackPos,
            currentHomeStep = token.homeStep,
            steps = state.diceValue,
            isClassic = isClassic
        ) ?: return state

        // Update moved token
        val updatedToken = token.copy(
            state = moveRes.newState,
            trackPos = moveRes.newTrackPos,
            homeStep = moveRes.newHomeStep
        )
        currentSideTokens[tokenIndex] = updatedToken

        val updatedAllTokens = state.allTokens.toMutableMap()
        updatedAllTokens[currentSide] = currentSideTokens

        var bannerMsg: String? = null
        var captureOccurred = false
        val isPlayerOne = currentSide == PlayerSide.RED

        val reachesGoal = updatedToken.isFinished && token.state != TokenState.FINISHED
        var updatedStats = state.stats.copy(
            playerStepsMoved = if (isPlayerOne) state.stats.playerStepsMoved + state.diceValue else state.stats.playerStepsMoved,
            opponentStepsMoved = if (!isPlayerOne) state.stats.opponentStepsMoved + state.diceValue else state.stats.opponentStepsMoved,
            playerTokensFinished = if (isPlayerOne && reachesGoal) state.stats.playerTokensFinished + 1 else state.stats.playerTokensFinished,
            opponentTokensFinished = if (!isPlayerOne && reachesGoal) state.stats.opponentTokensFinished + 1 else state.stats.opponentTokensFinished,
            turnsPlayed = state.stats.turnsPlayed + 1
        )

        // Check capture against all other active players if landing on TRACK and not a safe tile
        if (updatedToken.state == TokenState.TRACK && !GameBoardConstants.isSafeTile(updatedToken.trackPos)) {
            for (otherSide in state.activePlayers) {
                if (otherSide == currentSide) continue
                val opponentTokens = updatedAllTokens[otherSide]?.toMutableList() ?: continue
                for (i in opponentTokens.indices) {
                    val opponentToken = opponentTokens[i]
                    if (opponentToken.state == TokenState.TRACK && opponentToken.trackPos == updatedToken.trackPos) {
                        if (!isClassic && opponentToken.hasShield) {
                            // Shield blocks capture in Blitz
                            opponentTokens[i] = opponentToken.copy(hasShield = false)
                            bannerMsg = "🛡️ SHIELD BLOCKED CAPTURE!"
                        } else {
                            // Capture successful!
                            opponentTokens[i] = opponentToken.copy(
                                state = TokenState.BASE,
                                trackPos = 0,
                                homeStep = 0,
                                hasShield = false
                            )
                            captureOccurred = true
                            bannerMsg = "💥 ${currentSide.displayName.uppercase()} CAPTURED ${otherSide.displayName.uppercase()} TOKEN!"
                            updatedStats = if (isPlayerOne) {
                                updatedStats.copy(playerCaptures = updatedStats.playerCaptures + 1)
                            } else {
                                updatedStats.copy(opponentCaptures = updatedStats.opponentCaptures + 1)
                            }
                        }
                        updatedAllTokens[otherSide] = opponentTokens
                        break
                    }
                }
            }
        }

        // Check victory condition
        val currentFinished = currentSideTokens.all { it.isFinished }
        if (currentFinished) {
            val isHumanWinner = state.isPassAndPlay || (state.playerTypes[currentSide] == PlayerType.HUMAN)
            val result = if (isHumanWinner) BattleResult.VICTORY else BattleResult.DEFEAT
            val reason = "${currentSide.displayName} reached home with all 4 tokens!"
            return state.copy(
                allTokens = updatedAllTokens,
                isGameOver = true,
                winnerSide = currentSide,
                result = result,
                winReason = reason,
                stats = updatedStats,
                activeBannerMessage = "🏆 ${currentSide.displayName.uppercase()} WINS!"
            )
        }

        // Determine if player gets a bonus turn:
        // Rolling a 6, capturing an opponent token, or bringing a token home grants a bonus roll!
        val bonusTurn = (state.diceValue == 6 || captureOccurred || reachesGoal) && !state.isGameOver

        val nextTurn = if (bonusTurn) state.currentTurn else getNextPlayer(state.currentTurn, state.activePlayers)
        val nextConsecutiveSixes = if (bonusTurn && state.diceValue == 6) state.consecutiveSixes else 0

        val finalBanner = when {
            captureOccurred -> "${bannerMsg ?: "💥 CAPTURED!"} ⚡ BONUS ROLL!"
            reachesGoal -> "👑 ${currentSide.displayName.uppercase()} TOKEN HOME! ⚡ BONUS ROLL!"
            bonusTurn -> "⚡ BONUS ROLL EARNED!"
            else -> bannerMsg
        }

        return state.copy(
            allTokens = updatedAllTokens,
            currentTurn = nextTurn,
            turnTimeRemainingSeconds = if (isClassic) 15 else 8,
            hasRolled = false,
            isRolling = false,
            consecutiveSixes = nextConsecutiveSixes,
            selectableTokenIds = emptySet(),
            selectedTokenId = null,
            stats = updatedStats,
            activeBannerMessage = finalBanner
        )
    }

    fun getNextPlayer(current: PlayerSide, active: List<PlayerSide>): PlayerSide {
        val idx = active.indexOf(current)
        if (idx == -1 || active.isEmpty()) return active.firstOrNull() ?: PlayerSide.RED
        return active[(idx + 1) % active.size]
    }

    /**
     * Switches turn when turn timer expires or turn is passed
     */
    fun passTurn(state: GameState): GameState {
        if (state.isGameOver) return state
        val nextTurn = getNextPlayer(state.currentTurn, state.activePlayers)
        return state.copy(
            currentTurn = nextTurn,
            turnTimeRemainingSeconds = if (state.isClassic) 15 else 8,
            hasRolled = false,
            isRolling = false,
            consecutiveSixes = 0,
            selectableTokenIds = emptySet(),
            selectedTokenId = null,
            isDoubleDiceActive = false
        )
    }

    /**
     * Activates a power-up
     */
    fun activatePowerUp(state: GameState, powerUp: PowerUpType, side: PlayerSide): GameState {
        val isPlayer = side == PlayerSide.PLAYER
        val chargesMap = if (isPlayer) state.playerPowerUpCharges else state.opponentPowerUpCharges
        val chargesLeft = chargesMap[powerUp] ?: 0
        if (chargesLeft <= 0 || state.isGameOver) return state

        val updatedCharges = chargesMap.toMutableMap()
        updatedCharges[powerUp] = chargesLeft - 1

        val updatedStats = if (isPlayer) {
            state.stats.copy(playerPowerUpsUsed = state.stats.playerPowerUpsUsed + 1)
        } else {
            state.stats.copy(opponentPowerUpsUsed = state.stats.opponentPowerUpsUsed + 1)
        }

        var newState = if (isPlayer) {
            state.copy(playerPowerUpCharges = updatedCharges, stats = updatedStats)
        } else {
            state.copy(opponentPowerUpCharges = updatedCharges, stats = updatedStats)
        }

        when (powerUp) {
            PowerUpType.SPEED_BOOST -> {
                // Boost highest priority token by 2 spaces or add +2 to dice
                if (newState.hasRolled && !newState.isGameOver) {
                    val newDice = (newState.diceValue + 2).coerceAtMost(8)
                    val currentTokens = if (isPlayer) newState.playerTokens else newState.opponentTokens
                    val newLegal = findLegalMoveTokens(currentTokens, side, newDice)
                    newState = newState.copy(
                        diceValue = newDice,
                        selectableTokenIds = newLegal,
                        selectedTokenId = if (newLegal.size == 1) newLegal.first() else null,
                        activeBannerMessage = "⚡ SPEED BOOST (+2 DICE STEPS)!"
                    )
                } else {
                    val myTokens = if (isPlayer) newState.playerTokens.toMutableList() else newState.opponentTokens.toMutableList()
                    val targetToken = myTokens.filter { it.isOnTrack || it.isInHomeStretch }.maxByOrNull { it.totalProgress() }
                    if (targetToken != null) {
                        val idx = myTokens.indexOf(targetToken)
                        val moveRes = GameBoardConstants.calculateMove(side, targetToken.state, targetToken.trackPos, targetToken.homeStep, 2)
                        if (moveRes != null) {
                            val boosted = targetToken.copy(
                                state = moveRes.newState,
                                trackPos = moveRes.newTrackPos,
                                homeStep = moveRes.newHomeStep,
                                hasSpeedBoost = true
                            )
                            myTokens[idx] = boosted
                            val updatedTokens = newState.allTokens.toMutableMap()
                            updatedTokens[side] = myTokens
                            newState = newState.copy(allTokens = updatedTokens)
                            if (myTokens.all { it.isFinished }) {
                                val isHumanWin = newState.playerTypes[side] == PlayerType.HUMAN
                                return newState.copy(
                                    isGameOver = true,
                                    winnerSide = side,
                                    result = if (isHumanWin) BattleResult.VICTORY else BattleResult.DEFEAT,
                                    winReason = "${side.displayName} reached home with all 4 tokens!",
                                    activeBannerMessage = "🏆 ${side.displayName.uppercase()} WINS!"
                                )
                            }
                        }
                    }
                    newState = newState.copy(activeBannerMessage = "⚡ SPEED BOOST (+2 STEPS)!")
                }
            }
            PowerUpType.SHIELD -> {
                // Protect lead token on track with a shield
                val myTokens = (newState.allTokens[side] ?: emptyList()).toMutableList()
                val targetToken = myTokens.filter { it.isOnTrack && !it.hasShield }.maxByOrNull { it.totalProgress() }
                    ?: myTokens.firstOrNull { it.isOnTrack }
                if (targetToken != null) {
                    val idx = myTokens.indexOf(targetToken)
                    myTokens[idx] = targetToken.copy(hasShield = true)
                    val updatedTokens = newState.allTokens.toMutableMap()
                    updatedTokens[side] = myTokens
                    newState = newState.copy(allTokens = updatedTokens)
                }
                newState = newState.copy(activeBannerMessage = "🛡️ ENERGY SHIELD ACTIVATED!")
            }
            PowerUpType.DOUBLE_DICE -> {
                newState = newState.copy(isDoubleDiceActive = true, activeBannerMessage = "🎲 DOUBLE DICE ARMED!")
            }
            PowerUpType.EXTRA_TURN -> {
                // Grants extra turn
                newState = newState.copy(
                    hasRolled = false,
                    isRolling = false,
                    selectableTokenIds = emptySet(),
                    selectedTokenId = null,
                    turnTimeRemainingSeconds = 8,
                    activeBannerMessage = "⚡ EXTRA TURN GRANTED!"
                )
            }
            PowerUpType.SAFE_STEP -> {
                // Protects lead token with safe step immunity
                val myTokens = (newState.allTokens[side] ?: emptyList()).toMutableList()
                val targetToken = myTokens.filter { it.isOnTrack && !it.hasShield }.maxByOrNull { it.totalProgress() }
                    ?: myTokens.firstOrNull { it.isOnTrack }
                if (targetToken != null) {
                    val idx = myTokens.indexOf(targetToken)
                    myTokens[idx] = targetToken.copy(hasShield = true)
                    val updatedTokens = newState.allTokens.toMutableMap()
                    updatedTokens[side] = myTokens
                    newState = newState.copy(allTokens = updatedTokens)
                }
                newState = newState.copy(activeBannerMessage = "⭐ SAFE STEP ACTIVATED!")
            }
            PowerUpType.BLITZ_MOVE -> {
                // Blitz move advances lead token by 3 steps
                val myTokens = (newState.allTokens[side] ?: emptyList()).toMutableList()
                val targetToken = myTokens.filter { it.isOnTrack || it.isInHomeStretch }.maxByOrNull { it.totalProgress() }
                if (targetToken != null) {
                    val idx = myTokens.indexOf(targetToken)
                    val moveRes = GameBoardConstants.calculateMove(side, targetToken.state, targetToken.trackPos, targetToken.homeStep, 3)
                    if (moveRes != null) {
                        myTokens[idx] = targetToken.copy(
                            state = moveRes.newState,
                            trackPos = moveRes.newTrackPos,
                            homeStep = moveRes.newHomeStep
                        )
                        val updatedTokens = newState.allTokens.toMutableMap()
                        updatedTokens[side] = myTokens
                        newState = newState.copy(allTokens = updatedTokens)
                        if (myTokens.all { it.isFinished }) {
                            val isHumanWin = newState.playerTypes[side] == PlayerType.HUMAN
                            return newState.copy(
                                isGameOver = true,
                                winnerSide = side,
                                result = if (isHumanWin) BattleResult.VICTORY else BattleResult.DEFEAT,
                                winReason = "${side.displayName} reached home with all 4 tokens!",
                                activeBannerMessage = "🏆 ${side.displayName.uppercase()} WINS!"
                            )
                        }
                    }
                }
                newState = newState.copy(activeBannerMessage = "🚀 BLITZ MOVE (+3 STEPS)!")
            }
        }

        return newState
    }

    /**
     * Decrements the 60s match clock and handles intense countdown & final scoring
     */
    fun tickMatchTimer(state: GameState): GameState {
        if (state.isGameOver) return state

        val newRemaining = state.matchTimeRemainingSeconds - 1
        val newTurnTime = state.turnTimeRemainingSeconds - 1

        val isFinalTen = newRemaining in 1..10
        val countdownNum = if (newRemaining in 1..5) newRemaining else null

        var updatedState = state.copy(
            matchTimeRemainingSeconds = newRemaining.coerceAtLeast(0),
            turnTimeRemainingSeconds = newTurnTime.coerceAtLeast(0),
            isFinalTenSeconds = isFinalTen,
            countdownOverlayNumber = countdownNum
        )

        // Handle turn timeout
        if (newTurnTime <= 0 && newRemaining > 0) {
            updatedState = passTurn(updatedState)
        }

        // Handle 60s match expiration
        if (newRemaining <= 0) {
            updatedState = resolveTimeoutVictory(updatedState)
        }

        return updatedState
    }

    /**
     * Deterministic, transparent victory calculation at 60s timeout
     */
    fun resolveTimeoutVictory(state: GameState): GameState {
        val pScore = state.totalPlayerScore
        val oScore = state.totalOpponentScore

        val pFinished = state.playerTokens.count { it.isFinished }
        val oFinished = state.opponentTokens.count { it.isFinished }

        val pProgress = state.playerTokens.sumOf { it.totalProgress() }
        val oProgress = state.opponentTokens.sumOf { it.totalProgress() }

        val pCaptures = state.stats.playerCaptures
        val oCaptures = state.stats.opponentCaptures

        val result: BattleResult
        val reason: String

        if (pFinished != oFinished) {
            result = if (pFinished > oFinished) BattleResult.VICTORY else BattleResult.DEFEAT
            reason = if (pFinished > oFinished) "More tokens finished ($pFinished vs $oFinished)" else "Opponent finished more tokens ($oFinished vs $pFinished)"
        } else if (pProgress != oProgress) {
            result = if (pProgress > oProgress) BattleResult.VICTORY else BattleResult.DEFEAT
            reason = if (pProgress > oProgress) "Higher board progress ($pProgress vs $oProgress pts)" else "Opponent had higher progress ($oProgress vs $pProgress pts)"
        } else if (pCaptures != oCaptures) {
            result = if (pCaptures > oCaptures) BattleResult.VICTORY else BattleResult.DEFEAT
            reason = if (pCaptures > oCaptures) "More captures made ($pCaptures vs $oCaptures)" else "Opponent made more captures ($oCaptures vs $pCaptures)"
        } else if (pScore != oScore) {
            result = if (pScore > oScore) BattleResult.VICTORY else BattleResult.DEFEAT
            reason = if (pScore > oScore) "Higher overall battle score ($pScore vs $oScore)" else "Opponent scored higher ($oScore vs $pScore)"
        } else {
            result = BattleResult.DRAW
            reason = "Deadlock! Scores and positions tied ($pScore pts)"
        }

        return state.copy(
            isGameOver = true,
            result = result,
            winReason = reason,
            scorePlayer = pScore,
            scoreOpponent = oScore,
            activeBannerMessage = "⏰ TIME'S UP! FINAL SCORES TALLIED!"
        )
    }
}
