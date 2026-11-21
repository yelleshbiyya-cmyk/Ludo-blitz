package com.example.ludoblitz.model

data class MatchStats(
    val playerCaptures: Int = 0,
    val opponentCaptures: Int = 0,
    val playerTokensFinished: Int = 0,
    val opponentTokensFinished: Int = 0,
    val playerPowerUpsUsed: Int = 0,
    val opponentPowerUpsUsed: Int = 0,
    val playerStepsMoved: Int = 0,
    val opponentStepsMoved: Int = 0,
    val turnsPlayed: Int = 0
)

data class GameState(
    val mode: GameMode = GameMode.QUICK_BATTLE,
    val difficulty: AIDifficulty = AIDifficulty.MEDIUM,
    val matchTimeRemainingSeconds: Int = 60,
    val turnTimeRemainingSeconds: Int = 8,
    val currentTurn: PlayerSide = PlayerSide.RED,
    val diceValue: Int = 1,
    val hasRolled: Boolean = false,
    val isRolling: Boolean = false,
    val isDoubleDiceActive: Boolean = false,
    val secondDiceValue: Int = 0,

    val allTokens: Map<PlayerSide, List<Token>> = mapOf(
        PlayerSide.RED to List(4) { Token(id = it, side = PlayerSide.RED) },
        PlayerSide.YELLOW to List(4) { Token(id = it, side = PlayerSide.YELLOW) }
    ),

    val activePlayers: List<PlayerSide> = listOf(PlayerSide.RED, PlayerSide.YELLOW),
    val playerTypes: Map<PlayerSide, PlayerType> = mapOf(
        PlayerSide.RED to PlayerType.HUMAN,
        PlayerSide.YELLOW to PlayerType.AI,
        PlayerSide.GREEN to PlayerType.AI,
        PlayerSide.BLUE to PlayerType.AI
    ),
    val isPassAndPlay: Boolean = false,
    val consecutiveSixes: Int = 0,

    val selectableTokenIds: Set<Int> = emptySet(),
    val selectedTokenId: Int? = null,

    // Power-up charges remaining for current match (Blitz only)
    val playerPowerUpCharges: Map<PowerUpType, Int> = PowerUpType.entries.associateWith { it.maxChargesPerMatch },
    val opponentPowerUpCharges: Map<PowerUpType, Int> = PowerUpType.entries.associateWith { it.maxChargesPerMatch },

    // Active power-up announcement/animation
    val activeBannerMessage: String? = null,
    val lastPowerUpUsed: PowerUpType? = null,

    val stats: MatchStats = MatchStats(),

    val isGameOver: Boolean = false,
    val result: BattleResult? = null,
    val winnerSide: PlayerSide? = null,
    val winReason: String = "",
    val scorePlayer: Int = 0,
    val scoreOpponent: Int = 0,

    // Intense countdown trigger state (10s and 5..1)
    val isFinalTenSeconds: Boolean = false,
    val countdownOverlayNumber: Int? = null
) {
    val playerTokens: List<Token> get() = allTokens[PlayerSide.RED] ?: emptyList()
    val opponentTokens: List<Token> get() = allTokens[PlayerSide.YELLOW] ?: emptyList()

    val isClassic: Boolean get() = mode == GameMode.CLASSIC

    fun getTokens(side: PlayerSide): List<Token> = allTokens[side] ?: emptyList()

    fun isCurrentTurnHuman(): Boolean {
        if (isPassAndPlay) return true
        return (playerTypes[currentTurn] ?: PlayerType.AI) == PlayerType.HUMAN
    }

    fun hasPlayerWon(side: PlayerSide): Boolean {
        val tokens = getTokens(side)
        return tokens.isNotEmpty() && tokens.all { it.isFinished }
    }

    val totalPlayerScore: Int
        get() {
            val finished = playerTokens.count { it.isFinished } * 100
            val progress = playerTokens.sumOf { it.totalProgress() }
            val captures = stats.playerCaptures * 25
            return finished + progress + captures
        }

    val totalOpponentScore: Int
        get() {
            val finished = opponentTokens.count { it.isFinished } * 100
            val progress = opponentTokens.sumOf { it.totalProgress() }
            val captures = stats.opponentCaptures * 25
            return finished + progress + captures
        }
}
