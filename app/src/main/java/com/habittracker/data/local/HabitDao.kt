package com.habittracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE month = :month AND year = :year")
    fun getHabitsForMonth(month: Int, year: Int): Flow<List<HabitEntity>>

    @Upsert
    suspend fun upsertHabit(habit: HabitEntity)
    
    @Query("SELECT * FROM habits WHERE name = :name AND month = :month AND year = :year LIMIT 1")
    suspend fun getHabit(name: String, month: Int, year: Int): HabitEntity?

    @Query("DELETE FROM habits WHERE name = :name AND month = :month AND year = :year")
    suspend fun deleteHabit(name: String, month: Int, year: Int)
    
    @Query("SELECT * FROM habits WHERE isDirty = 1")
    suspend fun getDirtyHabits(): List<HabitEntity>
    
    @Query("UPDATE habits SET isDirty = 0 WHERE name = :name AND month = :month AND year = :year")
    suspend fun clearDirtyFlag(name: String, month: Int, year: Int)

    @Query("SELECT * FROM habits")
    fun getAllHabitsSync(): List<HabitEntity>
}
