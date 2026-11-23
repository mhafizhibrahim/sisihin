package com.sisihin.app.data.local

import androidx.compose.runtime.Immutable
import androidx.room.Embedded

@Immutable
data class GoalWithCollected(
    @Embedded val goal: SavingsGoalEntity,
    val collected: Long
)