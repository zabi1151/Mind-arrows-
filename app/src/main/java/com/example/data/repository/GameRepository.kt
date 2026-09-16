package com.example.data.repository

import android.content.Context
import com.example.data.db.MindArrowDatabase
import com.example.data.model.LevelProgressEntity
import com.example.data.model.UserStatsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

class GameRepository(context: Context) {
    private val db = MindArrowDatabase.getInstance(context)
    private val levelDao = db.levelProgressDao()
    private val statsDao = db.userStatsDao()

    val allProgressFlow: Flow<List<LevelProgressEntity>> = levelDao.getAllProgress()
    val statsFlow: Flow<UserStatsEntity> = statsDao.getUserStatsFlow().filterNotNull()

    suspend fun getStats(): UserStatsEntity = withContext(Dispatchers.IO) {
        statsDao.getUserStats() ?: UserStatsEntity().also { statsDao.insertOrUpdate(it) }
    }

    suspend fun completeLevel(
        levelId: Int,
        starsEarned: Int,
        timeSeconds: Int,
        movesTaken: Int
    ) = withContext(Dispatchers.IO) {
        val currentProgress = levelDao.getProgressForLevel(levelId)
        val bestStars = maxOf(starsEarned, currentProgress?.stars ?: 0)
        val bestTime = if (currentProgress?.bestTimeSeconds != null && currentProgress.bestTimeSeconds > 0) {
            minOf(timeSeconds, currentProgress.bestTimeSeconds)
        } else timeSeconds
        val bestMoves = if (currentProgress?.bestMoves != null && currentProgress.bestMoves > 0) {
            minOf(movesTaken, currentProgress.bestMoves)
        } else movesTaken

        levelDao.insertOrUpdate(
            LevelProgressEntity(
                levelId = levelId,
                isUnlocked = true,
                isCompleted = true,
                stars = bestStars,
                bestTimeSeconds = bestTime,
                bestMoves = bestMoves,
                completedTimestamp = System.currentTimeMillis()
            )
        )

        // Unlock next level if exists
        if (levelId < 300) {
            val nextProg = levelDao.getProgressForLevel(levelId + 1)
            if (nextProg == null || !nextProg.isUnlocked) {
                levelDao.insertOrUpdate(
                    LevelProgressEntity(
                        levelId = levelId + 1,
                        isUnlocked = true,
                        isCompleted = nextProg?.isCompleted ?: false,
                        stars = nextProg?.stars ?: 0
                    )
                )
            }
        }

        // Update user stats
        val currentStats = getStats()
        val isFirstTimeCompletion = currentProgress?.isCompleted != true
        val newCleared = if (isFirstTimeCompletion) currentStats.totalLevelsCleared + 1 else currentStats.totalLevelsCleared
        // Reduced reward coins: 15 base + 5 per star (max 30 coins/level)
        val newCoins = currentStats.coins + 15 + (starsEarned * 5)
        val newMoves = currentStats.totalMovesMade + movesTaken
        val newStars = if (isFirstTimeCompletion) currentStats.totalStars + starsEarned else currentStats.totalStars

        // Brain stats calculated organically from real gameplay performance
        val newSpatialScore = if (newCleared > 0) minOf(99, 65 + (newCleared * 2) + (newStars / 2)) else 0
        val newEfficiency = if (newCleared > 0) minOf(99, 78 + minOf(20, newStars)) else 0
        val newAvgTime = if (newCleared > 0) maxOf(12, timeSeconds) else 0

        statsDao.insertOrUpdate(
            currentStats.copy(
                totalLevelsCleared = newCleared,
                coins = newCoins,
                totalStars = newStars,
                totalMovesMade = newMoves,
                spatialLogicScore = newSpatialScore,
                moveEfficiencyPct = newEfficiency,
                avgTimeSeconds = newAvgTime
            )
        )
    }

    suspend fun buyArrowSkin(skinId: String, price: Int): Boolean = withContext(Dispatchers.IO) {
        val current = getStats()
        val unlockedList = current.unlockedArrowSkins.split(",").map { it.trim() }
        if (skinId in unlockedList) {
            // Already owned, just equip
            statsDao.insertOrUpdate(current.copy(equippedArrowSkin = skinId))
            return@withContext true
        }
        if (current.coins >= price) {
            val newUnlocked = (unlockedList + skinId).joinToString(",")
            statsDao.insertOrUpdate(
                current.copy(
                    coins = current.coins - price,
                    unlockedArrowSkins = newUnlocked,
                    equippedArrowSkin = skinId
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun equipArrowSkin(skinId: String): Boolean = withContext(Dispatchers.IO) {
        val current = getStats()
        val unlockedList = current.unlockedArrowSkins.split(",").map { it.trim() }
        if (skinId in unlockedList) {
            statsDao.insertOrUpdate(current.copy(equippedArrowSkin = skinId))
            true
        } else {
            false
        }
    }

    suspend fun claimDailyMission(missionId: String, rewardCoins: Int): Boolean = withContext(Dispatchers.IO) {
        val current = getStats()
        val claimedList = current.dailyMissionsClaimed.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (missionId !in claimedList) {
            val newClaimed = (claimedList + missionId).joinToString(",")
            statsDao.insertOrUpdate(
                current.copy(
                    coins = current.coins + rewardCoins,
                    dailyMissionsClaimed = newClaimed,
                    dailyStreak = maxOf(1, current.dailyStreak)
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun consumeHint(): Boolean = withContext(Dispatchers.IO) {
        val stats = getStats()
        if (stats.hintsRemaining > 0) {
            statsDao.insertOrUpdate(
                stats.copy(
                    hintsRemaining = stats.hintsRemaining - 1,
                    totalHintsUsed = stats.totalHintsUsed + 1
                )
            )
            true
        } else if (stats.coins >= 50) {
            // Auto buy hint with 50 coins if player wants
            statsDao.insertOrUpdate(
                stats.copy(
                    coins = stats.coins - 50,
                    totalHintsUsed = stats.totalHintsUsed + 1
                )
            )
            true
        } else {
            false
        }
    }

    suspend fun addHints(amount: Int) = withContext(Dispatchers.IO) {
        val stats = getStats()
        statsDao.insertOrUpdate(stats.copy(hintsRemaining = stats.hintsRemaining + amount))
    }

    suspend fun addCoins(amount: Int) = withContext(Dispatchers.IO) {
        val stats = getStats()
        statsDao.insertOrUpdate(stats.copy(coins = stats.coins + amount))
    }

    suspend fun recordUndo() = withContext(Dispatchers.IO) {
        val stats = getStats()
        statsDao.insertOrUpdate(stats.copy(totalUndosUsed = stats.totalUndosUsed + 1))
    }

    suspend fun updateSettings(
        sound: Boolean? = null,
        music: Boolean? = null,
        haptics: Boolean? = null,
        darkTheme: Boolean? = null
    ) = withContext(Dispatchers.IO) {
        val current = getStats()
        statsDao.insertOrUpdate(
            current.copy(
                soundEnabled = sound ?: current.soundEnabled,
                musicEnabled = music ?: current.musicEnabled,
                hapticsEnabled = haptics ?: current.hapticsEnabled,
                darkThemeEnabled = darkTheme ?: current.darkThemeEnabled
            )
        )
    }

    suspend fun setOnboardingSeen() = withContext(Dispatchers.IO) {
        val current = getStats()
        statsDao.insertOrUpdate(current.copy(hasSeenOnboarding = true))
    }

    suspend fun saveUserProfile(name: String, age: Int) = withContext(Dispatchers.IO) {
        val current = getStats()
        statsDao.insertOrUpdate(
            current.copy(
                userName = name.trim(),
                userAge = age,
                hasCompletedProfile = true
            )
        )
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        levelDao.deleteAll()
        val list = (1..300).map { i ->
            LevelProgressEntity(
                levelId = i,
                isUnlocked = (i == 1),
                isCompleted = false,
                stars = 0,
                bestTimeSeconds = 0,
                bestMoves = 0
            )
        }
        levelDao.insertAll(list)
        val current = getStats()
        statsDao.insertOrUpdate(
            UserStatsEntity(
                totalLevelsCleared = 0,
                totalStars = 0,
                coins = 0,
                hintsRemaining = 3,
                tokens = 0,
                dailyStreak = 0,
                spatialLogicScore = 0,
                moveEfficiencyPct = 0,
                avgTimeSeconds = 0,
                totalMovesMade = 0,
                totalHintsUsed = 0,
                totalUndosUsed = 0,
                equippedArrowSkin = "classic_cyan",
                unlockedArrowSkins = "classic_cyan",
                dailyMissionsClaimed = "",
                userName = current.userName,
                userAge = current.userAge,
                hasCompletedProfile = current.hasCompletedProfile
            )
        )
    }
}
