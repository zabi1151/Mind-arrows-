package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LevelProgressEntity
import com.example.data.model.UserStatsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress ORDER BY levelId ASC")
    suspend fun getAllProgressOnce(): List<LevelProgressEntity>

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    suspend fun getProgressForLevel(levelId: Int): LevelProgressEntity?

    @Query("SELECT * FROM level_progress WHERE levelId = :levelId")
    fun observeProgressForLevel(levelId: Int): Flow<LevelProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<LevelProgressEntity>)

    @Query("DELETE FROM level_progress")
    suspend fun deleteAll()
}

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)

    @Update
    suspend fun update(stats: UserStatsEntity)
}
