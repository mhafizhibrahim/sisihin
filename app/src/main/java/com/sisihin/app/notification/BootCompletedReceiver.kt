package com.sisihin.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.sisihin.app.data.local.SisihinDatabase
import kotlinx.coroutines.runBlocking

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val db = Room.databaseBuilder(context, SisihinDatabase::class.java, "sisihin.db").build()
        val scheduler = ReminderSchedulerImpl(context)
        runBlocking {
            db.goalDao().getAllOnce().filter { it.reminderEnabled }.forEach { scheduler.schedule(it) }
        }
        db.close()
    }
}