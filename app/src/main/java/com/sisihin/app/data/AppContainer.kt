package com.sisihin.app.data

import android.content.Context
import androidx.room.Room
import com.sisihin.app.data.local.SisihinDatabase
import com.sisihin.app.data.preferences.UserPreferencesDataStore
import com.sisihin.app.data.repository.SavingsRepository
import com.sisihin.app.notification.ReminderSchedulerImpl

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: SisihinDatabase = Room.databaseBuilder(appContext, SisihinDatabase::class.java, "sisihin.db").build()
    val prefs = UserPreferencesDataStore(appContext)
    val scheduler = ReminderSchedulerImpl(appContext)
    val repository = SavingsRepository(database.goalDao(), database.transactionDao(), scheduler)
}