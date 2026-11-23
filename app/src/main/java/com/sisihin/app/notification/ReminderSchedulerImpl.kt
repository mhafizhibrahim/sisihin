package com.sisihin.app.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.sisihin.app.data.local.SavingsGoalEntity

class ReminderSchedulerImpl(private val context: Context) : ReminderScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(goal: SavingsGoalEntity) {
        if (!goal.reminderEnabled) return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("goalId", goal.id)
            putExtra("title", goal.title)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            goal.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // Next occurrence today at reminderHour:Minute; if passed, next day
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, goal.reminderHour)
            set(java.util.Calendar.MINUTE, goal.reminderMinute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        } catch (e: SecurityException) {
            // fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
        }
    }

    override fun cancel(goalId: Long) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            goalId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pi)
    }
}