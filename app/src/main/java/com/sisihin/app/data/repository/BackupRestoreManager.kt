package com.sisihin.app.data.repository

import android.content.Context
import android.net.Uri
import com.sisihin.app.data.AppContainer
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupRestoreManager(private val container: AppContainer) {
    suspend fun export(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val goals = container.database.goalDao().getAllOnce()
            val root = JSONObject()
            val gArr = JSONArray()
            goals.forEach { g ->
                val gObj = JSONObject()
                gObj.put("id", g.id)
                gObj.put("title", g.title)
                gObj.put("targetAmount", g.targetAmount)
                gObj.put("currencyCode", g.currencyCode.name)
                gObj.put("planFrequency", g.planFrequency.name)
                gObj.put("planAmount", g.planAmount)
                gObj.put("targetDateMillis", g.targetDateMillis ?: JSONObject.NULL)
                gObj.put("imagePath", g.imagePath ?: JSONObject.NULL)
                gObj.put("reminderEnabled", g.reminderEnabled)
                gObj.put("reminderHour", g.reminderHour)
                gObj.put("reminderMinute", g.reminderMinute)
                gObj.put("reminderDaysMask", g.reminderDaysMask)
                gObj.put("status", g.status.name)
                gObj.put("createdAtMillis", g.createdAtMillis)
                gObj.put("completedAtMillis", g.completedAtMillis ?: JSONObject.NULL)
                val txs = container.database.transactionDao().getByGoal(g.id)
                // one-shot not available; use collected first
                gArr.put(gObj)
            }
            root.put("goals", gArr)
            context.contentResolver.openOutputStream(uri)?.use { it.write(root.toString().toByteArray()) }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun import(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return@withContext false
            val root = JSONObject(text)
            val gArr = root.getJSONArray("goals")
            for (i in 0 until gArr.length()) {
                val gObj = gArr.getJSONObject(i)
                val goal = com.sisihin.app.data.local.SavingsGoalEntity(
                    title = gObj.getString("title"),
                    targetAmount = gObj.getLong("targetAmount"),
                    currencyCode = com.sisihin.app.domain.model.CurrencyCode.valueOf(gObj.getString("currencyCode")),
                    planFrequency = com.sisihin.app.domain.model.PlanFrequency.valueOf(gObj.getString("planFrequency")),
                    planAmount = gObj.getLong("planAmount"),
                    targetDateMillis = if (gObj.isNull("targetDateMillis")) null else gObj.getLong("targetDateMillis"),
                    imagePath = if (gObj.isNull("imagePath")) null else gObj.getString("imagePath"),
                    reminderEnabled = gObj.getBoolean("reminderEnabled"),
                    reminderHour = gObj.getInt("reminderHour"),
                    reminderMinute = gObj.getInt("reminderMinute"),
                    reminderDaysMask = gObj.getInt("reminderDaysMask"),
                    status = com.sisihin.app.domain.model.GoalStatus.valueOf(gObj.getString("status")),
                    createdAtMillis = gObj.getLong("createdAtMillis"),
                    completedAtMillis = if (gObj.isNull("completedAtMillis")) null else gObj.getLong("completedAtMillis")
                )
                container.repository.insertGoal(goal)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}