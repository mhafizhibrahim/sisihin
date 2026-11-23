package com.sisihin.app.core.util

import com.sisihin.app.domain.model.PlanFrequency
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

object SavingsCalculator {
    fun remaining(target: Long, collected: Long): Long = (target - collected).coerceAtLeast(0L)

    fun percent(target: Long, collected: Long): Double {
        if (target <= 0L) return 0.0
        if (collected >= target) return 100.0
        val p = collected.toDouble() / target.toDouble() * 100.0
        return p.coerceIn(0.0, 99.0)
    }

    fun estimateText(remaining: Long, planAmount: Long, freq: PlanFrequency): String {
        if (remaining <= 0L) return "Target Tercapai"
        if (planAmount <= 0L) return "Atur rencana pengisian"
        val cycles = ceil(remaining.toDouble() / planAmount.toDouble()).toLong().coerceAtLeast(1L)
        return when (freq) {
            PlanFrequency.DAILY -> "$cycles Hari Lagi"
            PlanFrequency.WEEKLY -> "$cycles Minggu Lagi"
            PlanFrequency.MONTHLY -> "$cycles Bulan Lagi"
        }
    }

    fun daysTaken(createdMillis: Long, completedMillis: Long): Long {
        val created = LocalDate.ofEpochDay(createdMillis / 86400000L)
        val completed = LocalDate.ofEpochDay(completedMillis / 86400000L)
        return ChronoUnit.DAYS.between(created, completed).coerceAtLeast(1L)
    }

    fun cyclesForTarget(target: Long, planAmount: Long): Long {
        if (planAmount <= 0L) return 0L
        return ceil(target.toDouble() / planAmount.toDouble()).toLong()
    }

    fun dateFromCycles(freq: PlanFrequency, cycles: Long, startMillis: Long): Long {
        val days = when (freq) {
            PlanFrequency.DAILY -> cycles
            PlanFrequency.WEEKLY -> cycles * 7
            PlanFrequency.MONTHLY -> cycles * 30
        }
        return startMillis + days * 86400000L
    }
}