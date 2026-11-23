package com.sisihin.app.data.repository

import com.sisihin.app.core.util.ImageStorageManager
import com.sisihin.app.data.local.GoalDao
import com.sisihin.app.data.local.GoalWithCollected
import com.sisihin.app.data.local.SavingsGoalEntity
import com.sisihin.app.data.local.SavingsTransactionEntity
import com.sisihin.app.data.local.TransactionDao
import com.sisihin.app.domain.model.GoalStatus
import com.sisihin.app.notification.ReminderScheduler
import kotlinx.coroutines.flow.Flow

class SavingsRepository(
    private val goalDao: GoalDao,
    private val txDao: TransactionDao,
    private val scheduler: ReminderScheduler
) {
    fun goalsWithCollected(): Flow<List<GoalWithCollected>> = goalDao.getAllWithCollected()
    fun goalWithCollected(id: Long): Flow<GoalWithCollected?> = goalDao.getWithCollected(id)
    suspend fun getGoalOnce(id: Long): SavingsGoalEntity? = goalDao.getByIdOnce(id)
    fun transactions(goalId: Long): Flow<List<SavingsTransactionEntity>> = txDao.getByGoal(goalId)
    fun collectedAmount(goalId: Long): Flow<Long> = txDao.collectedAmount(goalId)

    suspend fun insertGoal(goal: SavingsGoalEntity): Long = goalDao.insert(goal)

    suspend fun updateGoal(goal: SavingsGoalEntity) {
        // Celengan terarsip read-only: form pengingat dan edit tidak boleh mengubahnya.
        if (isArchived(goal.id)) return
        goalDao.update(goal)
        refreshStatus(goal.id)
    }

    private suspend fun isArchived(goalId: Long): Boolean {
        val goal = goalDao.getByIdOnce(goalId) ?: return true
        if (goal.status == GoalStatus.ACHIEVED) return true
        return goal.targetAmount > 0L && txDao.collectedAmountOnce(goalId) >= goal.targetAmount
    }

    suspend fun deleteGoal(goal: SavingsGoalEntity) {
        ImageStorageManager.delete(goal.imagePath)
        goalDao.delete(goal)
        scheduler.cancel(goal.id)
    }

    /** Mengembalikan id transaksi baru supaya aksi "Urungkan" bisa menghapusnya; null jika diarsipkan. */
    suspend fun addTransaction(tx: SavingsTransactionEntity): Long? {
        if (isArchived(tx.goalId)) return null
        val id = txDao.insert(tx)
        refreshStatus(tx.goalId)
        return id
    }

    suspend fun updateTransaction(tx: SavingsTransactionEntity) {
        if (isArchived(tx.goalId)) return
        txDao.update(tx)
        refreshStatus(tx.goalId)
    }

    suspend fun deleteTransaction(tx: SavingsTransactionEntity) {
        if (isArchived(tx.goalId)) return
        txDao.delete(tx)
        refreshStatus(tx.goalId)
    }

    suspend fun deleteTransactionById(id: Long, goalId: Long) {
        txDao.delete(SavingsTransactionEntity(id = id, goalId = goalId, type = com.sisihin.app.domain.model.TransactionType.DEPOSIT, amount = 0L))
        refreshStatus(goalId)
    }

    private suspend fun refreshStatus(goalId: Long) {
        val goal = goalDao.getByIdOnce(goalId) ?: return
        val collected = txDao.collectedAmountOnce(goalId)
        val now = System.currentTimeMillis()
        var newGoal = goal
        if (collected >= goal.targetAmount && goal.targetAmount > 0 && goal.status == GoalStatus.ONGOING) {
            // Arsip: kunci data, matikan pengingat, dan batalkan alarm yang tertunda.
            newGoal = goal.copy(
                status = GoalStatus.ACHIEVED,
                completedAtMillis = now,
                reminderEnabled = false
            )
            scheduler.cancel(goalId)
        } else if (collected < goal.targetAmount && goal.status == GoalStatus.ACHIEVED) {
            newGoal = goal.copy(status = GoalStatus.ONGOING, completedAtMillis = null)
        }
        if (newGoal != goal) goalDao.update(newGoal)
        else if (newGoal.reminderEnabled && newGoal.status == GoalStatus.ONGOING) {
            scheduler.schedule(newGoal)
        }
    }
}
