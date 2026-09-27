package com.astro.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AstroDao {
    @Query("SELECT * FROM transactions ORDER BY createdAt DESC") fun transactions(): Flow<List<TransactionEntity>>
    @Insert suspend fun insertTransaction(item: TransactionEntity)
    @Delete suspend fun deleteTransaction(item: TransactionEntity)

    @Query("SELECT * FROM habits ORDER BY id DESC") fun habits(): Flow<List<HabitEntity>>
    @Insert suspend fun insertHabit(item: HabitEntity)
    @Update suspend fun updateHabit(item: HabitEntity)

    @Query("SELECT * FROM goals ORDER BY dueAt IS NULL, dueAt ASC") fun goals(): Flow<List<GoalEntity>>
    @Insert suspend fun insertGoal(item: GoalEntity)
    @Update suspend fun updateGoal(item: GoalEntity)

    @Query("SELECT * FROM categories ORDER BY name") fun categories(): Flow<List<CategoryEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertCategories(items: List<CategoryEntity>)
}
