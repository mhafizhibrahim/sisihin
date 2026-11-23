package com.sisihin.app.notification

import com.sisihin.app.data.local.SavingsGoalEntity

interface ReminderScheduler {
    fun schedule(goal: SavingsGoalEntity)
    fun cancel(goalId: Long)
}