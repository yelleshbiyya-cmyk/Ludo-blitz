package com.example.ludoblitz.service

import com.example.ludoblitz.model.PlayerProfile

data class MatchmakingResult(
    val matchId: String,
    val opponentName: String,
    val opponentAvatar: String,
    val opponentTrophies: Int,
    val opponentLevel: Int,
    val isRealTime: Boolean
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val trophies: Int,
    val league: String,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)

/**
 * Offline Arena League Ladder combining local bot rivals with the player's live trophy count.
 */
class ArenaLeaderboardService {
    private val rivalPool = listOf(
        LeaderboardEntry(0, "DragonSovereign", 3840, "Legend", "👑"),
        LeaderboardEntry(0, "SpeedDemon_X", 3620, "Legend", "⚡"),
        LeaderboardEntry(0, "CosmicValkyrie", 3450, "Legend", "🌌"),
        LeaderboardEntry(0, "ThunderRoll", 3210, "Legend", "🌩️"),
        LeaderboardEntry(0, "PhantomStriker", 3050, "Legend", "🥷"),
        LeaderboardEntry(0, "CyberPawn", 2890, "Master", "🤖"),
        LeaderboardEntry(0, "SolarisKing", 2740, "Master", "☀️"),
        LeaderboardEntry(0, "EmeraldGuard", 1450, "Gold", "🛡️"),
        LeaderboardEntry(0, "RubyRoller", 620, "Silver", "🔥")
    )

    fun getRankedStandings(profile: PlayerProfile): List<LeaderboardEntry> {
        val playerEntry = LeaderboardEntry(
            rank = 0,
            name = "${profile.username} (You)",
            trophies = profile.trophies,
            league = profile.currentLeague.title,
            avatarEmoji = "🦸",
            isCurrentUser = true
        )
        return (rivalPool + playerEntry)
            .sortedByDescending { it.trophies }
            .mapIndexed { index, entry -> entry.copy(rank = index + 1) }
    }
}
