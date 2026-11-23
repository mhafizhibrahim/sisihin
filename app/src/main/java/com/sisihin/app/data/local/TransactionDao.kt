package com.sisihin.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM savings_transactions WHERE goalId = :goalId ORDER BY timestampMillis DESC")
    fun getByGoal(goalId: Long): Flow<List<SavingsTransactionEntity>>

    @Query("SELECT COALESCE(SUM(CASE WHEN type='DEPOSIT' THEN amount ELSE 0 END),0) - COALESCE(SUM(CASE WHEN type='WITHDRAW' THEN amount ELSE 0 END),0) FROM savings_transactions WHERE goalId = :goalId")
    fun collectedAmount(goalId: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(CASE WHEN type='DEPOSIT' THEN amount ELSE 0 END),0) - COALESCE(SUM(CASE WHEN type='WITHDRAW' THEN amount ELSE 0 END),0) FROM savings_transactions WHERE goalId = :goalId")
    suspend fun collectedAmountOnce(goalId: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: SavingsTransactionEntity): Long

    @Update
    suspend fun update(tx: SavingsTransactionEntity)

    @Delete
    suspend fun delete(tx: SavingsTransactionEntity)
}