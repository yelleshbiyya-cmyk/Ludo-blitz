package com.example.ludoblitz.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    fun getPlayerProfileFlow(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProfile(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)
}

@Dao
interface MatchHistoryDao {
    @Query("SELECT * FROM match_history ORDER BY timestamp DESC LIMIT 30")
    fun getRecentMatches(): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchRecordEntity)

    @Query("DELETE FROM match_history")
    suspend fun clearHistory()
}

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests")
    fun getAllQuests(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests WHERE id = :questId LIMIT 1")
    suspend fun getQuestById(questId: String): QuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveQuests(quests: List<QuestEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateQuest(quest: QuestEntity)
}

@Dao
interface SavedGameDao {
    @Query("SELECT * FROM saved_game_state WHERE id = 1 LIMIT 1")
    fun getSavedGame(): Flow<SavedGameStateEntity?>

    @Query("SELECT * FROM saved_game_state WHERE id = 1 LIMIT 1")
    suspend fun getSavedGameOnce(): SavedGameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(game: SavedGameStateEntity)

    @Query("DELETE FROM saved_game_state WHERE id = 1")
    suspend fun clearSavedGame()
}
