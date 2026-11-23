package com.sisihin.app.presentation.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sisihin.app.data.AppContainer
import com.sisihin.app.data.local.SavingsGoalEntity
import com.sisihin.app.data.local.SavingsTransactionEntity
import com.sisihin.app.domain.model.GoalStatus
import com.sisihin.app.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class GoalDetailUiState(
    val goal: SavingsGoalEntity? = null,
    val collected: Long = 0L,
    val transactions: List<SavingsTransactionEntity> = emptyList()
) {
    /** Celengan yang sudah 100% masuk arsip read-only. */
    val isArchived: Boolean
        get() {
            val g = goal ?: return false
            if (g.status == GoalStatus.ACHIEVED) return true
            return g.targetAmount > 0L && collected >= g.targetAmount
        }

    /**
     * 3 pilihan nominal: rencana pengisian sebagai default, lalu nominal setoran
     * paling sering di celengan ini. Berubah otomatis mengikuti riwayat transaksi.
     */
    val suggestedAmounts: List<Long>
        get() {
            val g = goal ?: return emptyList()
            if (isArchived) return emptyList()
            val result = LinkedHashSet<Long>()
            if (g.planAmount > 0L) result.add(g.planAmount)
            transactions
                .asSequence()
                .filter { it.type == TransactionType.DEPOSIT && it.amount > 0L && it.amount != g.planAmount }
                .groupingBy { it.amount }
                .eachCount()
                .entries
                .sortedWith(compareByDescending<Map.Entry<Long, Int>> { it.value }.thenByDescending { it.key })
                .take(3 - result.size)
                .forEach { result.add(it.key) }
            return result.toList()
        }
}

class GoalDetailViewModel(private val container: AppContainer, val goalId: Long) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val f1 = container.repository.goalWithCollected(goalId)
            f1.collect { gwc ->
                _uiState.value = _uiState.value.copy(goal = gwc?.goal, collected = gwc?.collected ?: 0L)
            }
        }
        viewModelScope.launch {
            val f2 = container.repository.transactions(goalId)
            f2.collect { txs -> _uiState.value = _uiState.value.copy(transactions = txs) }
        }
    }

    fun deleteGoal(onDone: () -> Unit) = viewModelScope.launch {
        _uiState.value.goal?.let { container.repository.deleteGoal(it) }
        onDone()
    }

    /** Di arsip, riwayat tidak bisa diubah — termasuk penghapusan transaksi. */
    fun deleteTransaction(tx: SavingsTransactionEntity) = viewModelScope.launch {
        if (_uiState.value.isArchived) return@launch
        container.repository.deleteTransaction(tx)
    }

    fun addTransaction(tx: SavingsTransactionEntity) = viewModelScope.launch {
        if (_uiState.value.isArchived) return@launch
        container.repository.addTransaction(tx)
    }

    fun updateTransaction(tx: SavingsTransactionEntity) = viewModelScope.launch {
        if (_uiState.value.isArchived) return@launch
        container.repository.updateTransaction(tx)
    }

    /** Pengingat yang sudah mati di arsip tidak boleh dinyalakan lagi. */
    fun updateReminder(
        enabled: Boolean? = null,
        hour: Int? = null,
        minute: Int? = null,
        daysMask: Int? = null
    ) = viewModelScope.launch {
        val current = _uiState.value.goal ?: return@launch
        if (_uiState.value.isArchived) return@launch
        container.repository.updateGoal(
            current.copy(
                reminderEnabled = enabled ?: current.reminderEnabled,
                reminderHour = hour ?: current.reminderHour,
                reminderMinute = minute ?: current.reminderMinute,
                reminderDaysMask = daysMask ?: current.reminderDaysMask
            )
        )
    }

    /** Setoran dari chip nominal di halaman detail. */
    suspend fun depositAmount(amount: Long): Long? {
        val state = _uiState.value
        val goal = state.goal ?: return null
        if (state.isArchived || amount <= 0L) return null
        return container.repository.addTransaction(
            SavingsTransactionEntity(
                goalId = goal.id,
                type = TransactionType.DEPOSIT,
                amount = amount,
                note = "Setoran cepat"
            )
        )
    }

    /** Lewati guard arsip: ini pembatalan langsung atas setoran dari chip nominal. */
    fun undoDeposit(transactionId: Long) = viewModelScope.launch {
        container.repository.deleteTransactionById(transactionId, goalId)
    }
}
