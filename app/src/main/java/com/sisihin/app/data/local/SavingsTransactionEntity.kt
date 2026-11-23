package com.sisihin.app.data.local

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sisihin.app.domain.model.TransactionType

@Entity(
    tableName = "savings_transactions",
    foreignKeys = [ForeignKey(
        entity = SavingsGoalEntity::class,
        parentColumns = ["id"],
        childColumns = ["goalId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("goalId")]
)
@Immutable
data class SavingsTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val type: TransactionType,
    val amount: Long,
    val note: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)