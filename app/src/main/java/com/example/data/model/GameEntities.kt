package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelId: Int,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val bestTimeSeconds: Int = 0,
    val bestMoves: Int = 0,
    val completedTimestamp: Long = 0L
)

@Entity(tableName = "user_stats")
data class UserStatsEntity(
    @PrimaryKey val id: Int = 1,
    val totalLevelsCleared: Int = 0,
    val totalStars: Int = 0,
    val coins: Int = 0,
    val hintsRemaining: Int = 3,
    val tokens: Int = 0,
    val dailyStreak: Int = 0,
    val spatialLogicScore: Int = 0,
    val moveEfficiencyPct: Int = 0,
    val avgTimeSeconds: Int = 0,
    val totalMovesMade: Int = 0,
    val totalHintsUsed: Int = 0,
    val totalUndosUsed: Int = 0,
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val darkThemeEnabled: Boolean = false,
    val hasSeenOnboarding: Boolean = true,
    val lastDailyPlayedTimestamp: Long = 0L,
    val equippedArrowSkin: String = "classic_cyan",
    val unlockedArrowSkins: String = "classic_cyan",
    val dailyMissionsClaimed: String = "",
    val userName: String = "",
    val userAge: Int = 0,
    val hasCompletedProfile: Boolean = false
)
