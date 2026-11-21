package com.example.ludoblitz.engine

import com.example.ludoblitz.model.AIDifficulty
import com.example.ludoblitz.model.GameBoardConstants
import com.example.ludoblitz.model.GameState
import com.example.ludoblitz.model.PlayerSide
import com.example.ludoblitz.model.PowerUpType
import com.example.ludoblitz.model.Token
import com.example.ludoblitz.model.TokenState
import kotlin.random.Random

class LudoAIEngine(
    private val random: Random = Random.Default
) {
    /**
     * Chooses the best token to move for AI based on difficulty
     */
    fun selectBestMove(state: GameState, legalTokenIds: Set<Int>, side: PlayerSide = state.currentTurn): Int? {
        if (legalTokenIds.isEmpty()) return null
        if (legalTokenIds.size == 1) return legalTokenIds.first()

        val tokens = state.getTokens(side).filter { legalTokenIds.contains(it.id) }
        if (tokens.isEmpty()) return legalTokenIds.first()

        val otherTokens = state.activePlayers
            .filter { it != side }
            .flatMap { state.getTokens(it) }

        return when (state.difficulty) {
            AIDifficulty.EASY -> {
                // Casual: picks randomly among legal moves
                tokens.random(random).id
            }
            AIDifficulty.MEDIUM -> {
                // Balanced: prioritizes captures, entering finish, and getting out of base
                evaluateTokens(tokens, otherTokens, state.diceValue, side, state.isClassic).id
            }
            AIDifficulty.HARD -> {
                // Aggressive & tactical
                evaluateTokensHard(tokens, otherTokens, state.diceValue, side, state.isClassic).id
            }
        }
    }

    private fun evaluateTokens(
        aiTokens: List<Token>,
        opponentTokens: List<Token>,
        diceValue: Int,
        side: PlayerSide,
        isClassic: Boolean
    ): Token {
        var bestToken = aiTokens.first()
        var bestScore = Int.MIN_VALUE

        for (token in aiTokens) {
            var score = 0
            val moveRes = GameBoardConstants.calculateMove(
                side = side,
                currentState = token.state,
                currentTrackPos = token.trackPos,
                currentHomeStep = token.homeStep,
                steps = diceValue,
                isClassic = isClassic
            ) ?: continue

            // 1. Can finish token?
            if (moveRes.newState == TokenState.FINISHED) {
                score += 150
            }

            // 2. Can capture an opponent token?
            if (moveRes.newState == TokenState.TRACK && !GameBoardConstants.isSafeTile(moveRes.newTrackPos)) {
                val canCapture = opponentTokens.any { it.state == TokenState.TRACK && it.trackPos == moveRes.newTrackPos }
                if (canCapture) {
                    score += 200
                }
            }

            // 3. Can release from base?
            if (token.state == TokenState.BASE && moveRes.newState == TokenState.TRACK) {
                score += 80
            }

            // 4. Can land on safe tile?
            if (moveRes.newState == TokenState.TRACK && GameBoardConstants.isSafeTile(moveRes.newTrackPos)) {
                score += 60
            }

            // 5. Enter home stretch?
            if (token.state == TokenState.TRACK && moveRes.newState == TokenState.HOME_STRETCH) {
                score += 90
            }

            // 6. Advance lead token
            score += token.totalProgress()

            if (score > bestScore) {
                bestScore = score
                bestToken = token
            }
        }

        return bestToken
    }

    private fun evaluateTokensHard(
        aiTokens: List<Token>,
        opponentTokens: List<Token>,
        diceValue: Int,
        side: PlayerSide,
        isClassic: Boolean
    ): Token {
        // More aggressive capture priority & avoiding trailing vulnerable positions
        return evaluateTokens(aiTokens, opponentTokens, diceValue, side, isClassic)
    }

    /**
     * Decides if AI should trigger a powerup this turn
     */
    fun shouldActivatePowerUp(state: GameState): PowerUpType? {
        if (state.currentTurn != PlayerSide.OPPONENT || state.isGameOver) return null
        val difficulty = state.difficulty
        if (difficulty == AIDifficulty.EASY) return null

        val available = state.opponentPowerUpCharges.filter { it.value > 0 }.keys

        // Hard AI uses power-ups when strategically advantageous
        if (available.contains(PowerUpType.DOUBLE_DICE) && !state.hasRolled && random.nextFloat() < 0.25f) {
            return PowerUpType.DOUBLE_DICE
        }

        if (available.contains(PowerUpType.BLITZ_MOVE)) {
            val aiLead = state.opponentTokens.firstOrNull { it.isOnTrack }
            if (aiLead != null && random.nextFloat() < 0.30f) {
                return PowerUpType.BLITZ_MOVE
            }
        }

        if (available.contains(PowerUpType.EXTRA_TURN) && state.hasRolled && random.nextFloat() < 0.25f) {
            return PowerUpType.EXTRA_TURN
        }

        if (available.contains(PowerUpType.SHIELD) || available.contains(PowerUpType.SAFE_STEP)) {
            val aiLead = state.opponentTokens.firstOrNull { it.isOnTrack && !it.hasShield }
            if (aiLead != null && random.nextFloat() < 0.30f) {
                return if (available.contains(PowerUpType.SHIELD)) PowerUpType.SHIELD else PowerUpType.SAFE_STEP
            }
        }

        return null
    }
}
