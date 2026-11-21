package com.example.ludoblitz.model

data class GridCoord(val row: Float, val col: Float)

object GameBoardConstants {
    const val TOTAL_TRACK_TILES = 52
    const val HOME_LANE_LENGTH = 5 // Steps 0..4, step 5 is FINISHED (Center)
    const val TOTAL_GOAL_STEPS = 57 // 51 track steps from start + 5 home lane steps + 1 goal step

    // Standard 8 Safe Cells on a 52-cell Ludo board (4 Starting squares + 4 Star squares)
    val SAFE_TILES = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    // Player (Red - Bottom Left)
    const val PLAYER_START_TILE = 0
    const val PLAYER_HOME_GATE_TILE = 51 // Enters Red home lane after passing tile 51

    // Opponent (Yellow - Top Right)
    const val OPPONENT_START_TILE = 26
    const val OPPONENT_HOME_GATE_TILE = 25 // Enters Yellow home lane after passing tile 25

    // 52 Track cells mapped to 15x15 grid coordinates (row 0..14, col 0..14)
    val TRACK_COORDS = listOf(
        // Red start and bottom-left arm moving up
        GridCoord(13f, 6f), // 0 [RED START - SAFE]
        GridCoord(12f, 6f), // 1
        GridCoord(11f, 6f), // 2
        GridCoord(10f, 6f), // 3
        GridCoord(9f, 6f),  // 4
        // Left arm moving left
        GridCoord(8f, 5f),  // 5
        GridCoord(8f, 4f),  // 6
        GridCoord(8f, 3f),  // 7
        GridCoord(8f, 2f),  // 8 [SAFE STAR]
        GridCoord(8f, 1f),  // 9
        GridCoord(8f, 0f),  // 10
        GridCoord(7f, 0f),  // 11
        GridCoord(6f, 0f),  // 12
        // Green start & left arm moving right
        GridCoord(6f, 1f),  // 13 [GREEN START - SAFE]
        GridCoord(6f, 2f),  // 14
        GridCoord(6f, 3f),  // 15
        GridCoord(6f, 4f),  // 16
        GridCoord(6f, 5f),  // 17
        // Top arm moving up
        GridCoord(5f, 6f),  // 18
        GridCoord(4f, 6f),  // 19
        GridCoord(3f, 6f),  // 20
        GridCoord(2f, 6f),  // 21 [SAFE STAR]
        GridCoord(1f, 6f),  // 22
        GridCoord(0f, 6f),  // 23
        GridCoord(0f, 7f),  // 24
        GridCoord(0f, 8f),  // 25
        // Yellow start & top arm moving down
        GridCoord(1f, 8f),  // 26 [YELLOW START - SAFE]
        GridCoord(2f, 8f),  // 27
        GridCoord(3f, 8f),  // 28
        GridCoord(4f, 8f),  // 29
        GridCoord(5f, 8f),  // 30
        // Right arm moving right
        GridCoord(6f, 9f),  // 31
        GridCoord(6f, 10f), // 32
        GridCoord(6f, 11f), // 33
        GridCoord(6f, 12f), // 34 [SAFE STAR]
        GridCoord(6f, 13f), // 35
        GridCoord(6f, 14f), // 36
        GridCoord(7f, 14f), // 37
        GridCoord(8f, 14f), // 38
        // Blue start & right arm moving left
        GridCoord(8f, 13f), // 39 [BLUE START - SAFE]
        GridCoord(8f, 12f), // 40
        GridCoord(8f, 11f), // 41
        GridCoord(8f, 10f), // 42
        GridCoord(8f, 9f),  // 43
        // Bottom arm moving down
        GridCoord(9f, 8f),  // 44
        GridCoord(10f, 8f), // 45
        GridCoord(11f, 8f), // 46
        GridCoord(12f, 8f), // 47 [SAFE STAR]
        GridCoord(13f, 8f), // 48
        GridCoord(14f, 8f), // 49
        GridCoord(14f, 7f), // 50
        GridCoord(14f, 6f)  // 51 [Last cell before Red Home Lane]
    )

    // Red Home Lane (steps 0..4 lead to center)
    val RED_HOME_LANE = listOf(
        GridCoord(13f, 7f),
        GridCoord(12f, 7f),
        GridCoord(11f, 7f),
        GridCoord(10f, 7f),
        GridCoord(9f, 7f)
    )

    // Yellow Home Lane (steps 0..4 lead to center)
    val YELLOW_HOME_LANE = listOf(
        GridCoord(1f, 7f),
        GridCoord(2f, 7f),
        GridCoord(3f, 7f),
        GridCoord(4f, 7f),
        GridCoord(5f, 7f)
    )

    // Green Home Lane (steps 0..4 lead to center)
    val GREEN_HOME_LANE = listOf(
        GridCoord(7f, 1f),
        GridCoord(7f, 2f),
        GridCoord(7f, 3f),
        GridCoord(7f, 4f),
        GridCoord(7f, 5f)
    )

    // Blue Home Lane (steps 0..4 lead to center)
    val BLUE_HOME_LANE = listOf(
        GridCoord(7f, 13f),
        GridCoord(7f, 12f),
        GridCoord(7f, 11f),
        GridCoord(7f, 10f),
        GridCoord(7f, 9f)
    )

    // Base Nests for 4 tokens per color
    val RED_BASE_NESTS = listOf(
        GridCoord(10.5f, 1.5f),
        GridCoord(10.5f, 3.5f),
        GridCoord(12.5f, 1.5f),
        GridCoord(12.5f, 3.5f)
    )

    val YELLOW_BASE_NESTS = listOf(
        GridCoord(1.5f, 10.5f),
        GridCoord(1.5f, 12.5f),
        GridCoord(3.5f, 10.5f),
        GridCoord(3.5f, 12.5f)
    )

    val GREEN_BASE_NESTS = listOf(
        GridCoord(1.5f, 1.5f),
        GridCoord(1.5f, 3.5f),
        GridCoord(3.5f, 1.5f),
        GridCoord(3.5f, 3.5f)
    )

    val BLUE_BASE_NESTS = listOf(
        GridCoord(10.5f, 10.5f),
        GridCoord(10.5f, 12.5f),
        GridCoord(12.5f, 10.5f),
        GridCoord(12.5f, 12.5f)
    )

    // Center Finish Goal
    val CENTER_FINISH = GridCoord(7f, 7f)

    fun isSafeTile(tilePos: Int): Boolean {
        return SAFE_TILES.contains(tilePos)
    }

    /**
     * Calculates new position given current track position and dice roll steps.
     * Returns a MoveResult or null if move is impossible/overshot.
     */
    fun calculateMove(
        side: PlayerSide,
        currentState: TokenState,
        currentTrackPos: Int,
        currentHomeStep: Int,
        steps: Int,
        isClassic: Boolean = false
    ): MoveResult? {
        when (currentState) {
            TokenState.BASE -> {
                // In Classic: rolling a 6 enters track. In Blitz: 6 or 1 enters track.
                val canExit = if (isClassic) steps == 6 else (steps == 6 || steps == 1)
                if (canExit) {
                    return MoveResult(TokenState.TRACK, side.startTile, 0)
                }
                return null
            }
            TokenState.TRACK -> {
                val startPos = side.startTile
                // Calculate distance traveled on track from starting tile
                var currentDist = currentTrackPos - startPos
                if (currentDist < 0) currentDist += TOTAL_TRACK_TILES
                val newDist = currentDist + steps

                if (newDist <= 50) {
                    // Still on standard 52-cell track
                    val newTrackPos = (currentTrackPos + steps) % TOTAL_TRACK_TILES
                    return MoveResult(TokenState.TRACK, newTrackPos, 0)
                } else {
                    // Turning into colored home lane (51 track cells traveled)
                    val homeIndex = newDist - 51
                    if (homeIndex < HOME_LANE_LENGTH) {
                        return MoveResult(TokenState.HOME_STRETCH, currentTrackPos, homeIndex)
                    } else if (homeIndex == HOME_LANE_LENGTH) {
                        // Landed perfectly into Center Finish Goal!
                        return MoveResult(TokenState.FINISHED, currentTrackPos, HOME_LANE_LENGTH)
                    } else {
                        // Overshot the finish center goal
                        return null
                    }
                }
            }
            TokenState.HOME_STRETCH -> {
                val newHomeStep = currentHomeStep + steps
                if (newHomeStep < HOME_LANE_LENGTH) {
                    return MoveResult(TokenState.HOME_STRETCH, currentTrackPos, newHomeStep)
                } else if (newHomeStep == HOME_LANE_LENGTH) {
                    return MoveResult(TokenState.FINISHED, currentTrackPos, HOME_LANE_LENGTH)
                } else {
                    return null // Overshot
                }
            }
            TokenState.FINISHED -> return null
        }
    }

    /**
     * Converts a token's current state to exact (row, col) grid coordinates on the 15x15 board.
     */
    fun getTokenGridCoord(token: Token): GridCoord {
        return when (token.state) {
            TokenState.BASE -> {
                when (token.side) {
                    PlayerSide.RED -> RED_BASE_NESTS.getOrElse(token.id) { GridCoord(11.5f, 2.5f) }
                    PlayerSide.GREEN -> GREEN_BASE_NESTS.getOrElse(token.id) { GridCoord(2.5f, 2.5f) }
                    PlayerSide.YELLOW -> YELLOW_BASE_NESTS.getOrElse(token.id) { GridCoord(2.5f, 11.5f) }
                    PlayerSide.BLUE -> BLUE_BASE_NESTS.getOrElse(token.id) { GridCoord(11.5f, 11.5f) }
                }
            }
            TokenState.TRACK -> {
                TRACK_COORDS.getOrElse(token.trackPos) { GridCoord(7f, 7f) }
            }
            TokenState.HOME_STRETCH -> {
                when (token.side) {
                    PlayerSide.RED -> RED_HOME_LANE.getOrElse(token.homeStep) { GridCoord(7f, 7f) }
                    PlayerSide.GREEN -> GREEN_HOME_LANE.getOrElse(token.homeStep) { GridCoord(7f, 7f) }
                    PlayerSide.YELLOW -> YELLOW_HOME_LANE.getOrElse(token.homeStep) { GridCoord(7f, 7f) }
                    PlayerSide.BLUE -> BLUE_HOME_LANE.getOrElse(token.homeStep) { GridCoord(7f, 7f) }
                }
            }
            TokenState.FINISHED -> {
                // In center finish goal, offset based on color and token ID
                val offsetX = when (token.id) {
                    0 -> -0.25f
                    1 -> 0.25f
                    2 -> -0.25f
                    else -> 0.25f
                }
                val offsetY = when (token.id) {
                    0 -> -0.25f
                    1 -> -0.25f
                    2 -> 0.25f
                    else -> 0.25f
                }
                val (centerRowOffset, centerColOffset) = when (token.side) {
                    PlayerSide.RED -> Pair(0.35f, 0f)
                    PlayerSide.GREEN -> Pair(0f, -0.35f)
                    PlayerSide.YELLOW -> Pair(-0.35f, 0f)
                    PlayerSide.BLUE -> Pair(0f, 0.35f)
                }
                GridCoord(7f + centerRowOffset + (offsetY * 0.3f), 7f + centerColOffset + (offsetX * 0.3f))
            }
        }
    }
}

data class MoveResult(
    val newState: TokenState,
    val newTrackPos: Int,
    val newHomeStep: Int
)

