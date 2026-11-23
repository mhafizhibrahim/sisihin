package com.sisihin.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM savings_goals ORDER BY createdAtMillis DESC")
    fun getAll(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals ORDER BY createdAtMillis DESC")
    suspend fun getAllOnce(): List<SavingsGoalEntity>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    fun getById(id: Long): Flow<SavingsGoalEntity?>

    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getByIdOnce(id: Long): SavingsGoalEntity?

    @Query("""
        SELECT g.*, COALESCE((SELECT SUM(CASE WHEN t.type='DEPOSIT' THEN t.amount ELSE 0 END) - SUM(CASE WHEN t.type='WITHDRAW' THEN t.amount ELSE 0 END) FROM savings_transactions t WHERE t.goalId = g.id),0) AS collected
        FROM savings_goals g
        ORDER BY g.createdAtMillis DESC
    """)
    fun getAllWithCollected(): Flow<List<GoalWithCollected>>

    @Query("""
        SELECT g.*, COALESCE((SELECT SUM(CASE WHEN t.type='DEPOSIT' THEN t.amount ELSE 0 END) - SUM(CASE WHEN t.type='WITHDRAW' THEN t.amount ELSE 0 END) FROM savings_transactions t WHERE t.goalId = g.id),0) AS collected
        FROM savings_goals g
        WHERE g.id = :id
    """)
    fun getWithCollected(id: Long): Flow<GoalWithCollected?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: SavingsGoalEntity): Long

    @Update
    suspend fun update(goal: SavingsGoalEntity)

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}