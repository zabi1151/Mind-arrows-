package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.LevelProgressDao
import com.example.data.dao.UserStatsDao
import com.example.data.model.LevelProgressEntity
import com.example.data.model.UserStatsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [LevelProgressEntity::class, UserStatsEntity::class],
    version = 3,
    exportSchema = false
)
abstract class MindArrowDatabase : RoomDatabase() {
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: MindArrowDatabase? = null

        fun getInstance(context: Context): MindArrowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MindArrowDatabase::class.java,
                    "mind_arrows.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                ensureAllLevelsExist(getInstance(context))
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(database: MindArrowDatabase) {
            val statsDao = database.userStatsDao()
            val levelDao = database.levelProgressDao()

            if (statsDao.getUserStats() == null) {
                statsDao.insertOrUpdate(UserStatsEntity())
            }

            // Populate levels 1..300; ONLY Level 1 is unlocked from scratch!
            val list = mutableListOf<LevelProgressEntity>()
            for (i in 1..300) {
                val isUnlocked = (i == 1)
                val isCompleted = false
                val stars = 0
                val moves = 0
                val time = 0

                list.add(
                    LevelProgressEntity(
                        levelId = i,
                        isUnlocked = isUnlocked,
                        isCompleted = isCompleted,
                        stars = stars,
                        bestTimeSeconds = time,
                        bestMoves = moves,
                        completedTimestamp = 0L
                    )
                )
            }
            levelDao.insertAll(list)
        }

        private suspend fun ensureAllLevelsExist(database: MindArrowDatabase) {
            val levelDao = database.levelProgressDao()
            val existing = levelDao.getAllProgressOnce()
            if (existing.size < 300) {
                val existingIds = existing.map { it.levelId }.toSet()
                val missing = mutableListOf<LevelProgressEntity>()
                for (i in 1..300) {
                    if (i !in existingIds) {
                        missing.add(
                            LevelProgressEntity(
                                levelId = i,
                                isUnlocked = false,
                                isCompleted = false,
                                stars = 0,
                                bestTimeSeconds = 0,
                                bestMoves = 0,
                                completedTimestamp = 0L
                            )
                        )
                    }
                }
                if (missing.isNotEmpty()) {
                    levelDao.insertAll(missing)
                }
            }
        }
    }
}
