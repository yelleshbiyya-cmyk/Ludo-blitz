package com.example.ludoblitz.model

data class Token(
    val id: Int, // 0..3
    val side: PlayerSide,
    val state: TokenState = TokenState.BASE,
    val trackPos: Int = 0, // 0..51 on standard track
    val homeStep: Int = 0, // 0..4 inside colored home lane (step 5 is FINISHED)
    val hasShield: Boolean = false,
    val hasSpeedBoost: Boolean = false,
    val isHighlighted: Boolean = false
) {
    val isFinished: Boolean get() = state == TokenState.FINISHED
    val isInBase: Boolean get() = state == TokenState.BASE
    val isOnTrack: Boolean get() = state == TokenState.TRACK
    val isInHomeStretch: Boolean get() = state == TokenState.HOME_STRETCH

    /**
     * Total steps travelled from start line (0 to 57)
     */
    fun totalProgress(): Int {
        return when (state) {
            TokenState.BASE -> 0
            TokenState.TRACK -> {
                val startPos = side.startTile
                var diff = trackPos - startPos
                if (diff < 0) diff += GameBoardConstants.TOTAL_TRACK_TILES
                diff + 1 // 1..51
            }
            TokenState.HOME_STRETCH -> 51 + homeStep + 1 // 52..56
            TokenState.FINISHED -> 57
        }
    }
}

