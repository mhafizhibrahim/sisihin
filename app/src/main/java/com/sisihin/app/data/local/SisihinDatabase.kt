package com.sisihin.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [SavingsGoalEntity::class, SavingsTransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SisihinDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun transactionDao(): TransactionDao
}