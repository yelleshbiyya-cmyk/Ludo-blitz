package com.example.ludoblitz.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PlayerProfileEntity::class,
        MatchRecordEntity::class,
        QuestEntity::class,
        SavedGameStateEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun matchHistoryDao(): MatchHistoryDao
    abstract fun questDao(): QuestDao
    abstract fun savedGameDao(): SavedGameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Safe non-destructive migration from schema v1 -> v2 (adds saved_game_state).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `saved_game_state` (
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
            }
        }

        /**
         * Safe non-destructive migration from schema v2 -> v3:
         * Preserves all player progress, stats, quests, match history, and saved games while adding
         * profile frame selection, separate Music/SFX volume controls, claimed level milestones,
         * total finished tokens, and weekly mission reset timestamp.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `selectedProfileFrameId` TEXT NOT NULL DEFAULT 'frame_classic'")
                db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `musicVolume` REAL NOT NULL DEFAULT 0.7")
                db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `sfxVolume` REAL NOT NULL DEFAULT 1.0")
                db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `claimedMilestonesCsv` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `tokensFinished` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `player_profile` ADD COLUMN `weeklyResetTimestamp` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ludo_blitz_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
