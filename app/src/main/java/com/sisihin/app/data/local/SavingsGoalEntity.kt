package com.sisihin.app.data.local

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.GoalStatus
import com.sisihin.app.domain.model.PlanFrequency

@Immutable
@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Long,
    val currencyCode: CurrencyCode = CurrencyCode.IDR,
    val planFrequency: PlanFrequency = PlanFrequency.DAILY,
    val planAmount: Long = 0,
    val targetDateMillis: Long? = null,
    val imagePath: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 12,
    val reminderMinute: Int = 0,
    val reminderDaysMask: Int = 0,
    val status: GoalStatus = GoalStatus.ONGOING,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val completedAtMillis: Long? = null
)