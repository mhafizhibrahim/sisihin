package com.sisihin.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val goalId = intent.getLongExtra("goalId", -1L)
        val title = intent.getStringExtra("title") ?: "Sisihin"
        NotificationHelper.show(context, goalId.toInt(), "Waktunya menabung", "Lanjutkan target: $title")
    }
}