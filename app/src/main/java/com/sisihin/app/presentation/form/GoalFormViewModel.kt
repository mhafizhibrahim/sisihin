package com.sisihin.app.presentation.form

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.core.util.ImageStorageManager
import com.sisihin.app.data.AppContainer
import com.sisihin.app.data.local.SavingsGoalEntity
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.GoalStatus
import com.sisihin.app.domain.model.PlanFrequency
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Immutable
data class GoalFormUiState(
    val editingId: Long? = null,
    val title: String = "",
    val targetAmountText: TextFieldValue = TextFieldValue(""),
    val currency: CurrencyCode = CurrencyCode.IDR,
    val planFrequency: PlanFrequency = PlanFrequency.DAILY,
    val planAmountText: TextFieldValue = TextFieldValue(""),
    val targetDateMillis: Long? = null,
    val imagePath: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 12,
    val reminderMinute: Int = 0,
    val reminderDaysMask: Int = 0,
    val saving: Boolean = false
)

class GoalFormViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(GoalFormUiState())
    val uiState: StateFlow<GoalFormUiState> = _uiState.asStateFlow()

    /** Alias lama untuk kompatibilitas. */
    @Deprecated("Gunakan uiState", ReplaceWith("uiState"))
    val state: StateFlow<GoalFormUiState> get() = uiState

    fun load(goalId: Long) = viewModelScope.launch {
        val goal = container.repository.getGoalOnce(goalId)
        goal?.let { g ->
            _uiState.value = GoalFormUiState(
                editingId = g.id,
                title = g.title,
                targetAmountText = CurrencyFormatter.inputValue(g.targetAmount),
                currency = g.currencyCode,
                planFrequency = g.planFrequency,
                planAmountText = CurrencyFormatter.inputValue(g.planAmount),
                targetDateMillis = g.targetDateMillis,
                imagePath = g.imagePath,
                reminderEnabled = g.reminderEnabled,
                reminderHour = g.reminderHour,
                reminderMinute = g.reminderMinute,
                reminderDaysMask = g.reminderDaysMask
            )
        }
    }

    /** Method reference: menjaga holdability lambda dan menekan recomposition. */
    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onTargetAmountChange(value: TextFieldValue) {
        _uiState.value = _uiState.value.copy(targetAmountText = CurrencyFormatter.formatInput(value))
    }

    fun onPlanAmountChange(value: TextFieldValue) {
        _uiState.value = _uiState.value.copy(planAmountText = CurrencyFormatter.formatInput(value))
    }

    fun setCurrency(value: CurrencyCode) {
        _uiState.value = _uiState.value.copy(currency = value)
    }

    fun setPlanFrequency(value: PlanFrequency) {
        _uiState.value = _uiState.value.copy(planFrequency = value)
    }

    fun setReminderEnabled(enabled: Boolean) {
        val current = _uiState.value
        _uiState.value = current.copy(
            reminderEnabled = enabled,
            reminderDaysMask = if (enabled && current.reminderDaysMask == 0) ALL_DAYS_MASK else current.reminderDaysMask
        )
    }

    fun setReminderTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(
            reminderHour = hour.coerceIn(0, 23),
            reminderMinute = minute.coerceIn(0, 59)
        )
    }

    fun toggleReminderDay(bit: Int) {
        val current = _uiState.value
        _uiState.value = current.copy(reminderDaysMask = current.reminderDaysMask xor bit)
    }

    /**
     * Mengisi nominal otomatis dari tanggal target yang dipilih.
     * @return false bila Target Tabungan masih kosong (dipakai UI untuk snackbar).
     */
    fun setTargetDateFromPicker(dateMillis: Long): Boolean {
        val current = _uiState.value
        val target = CurrencyFormatter.parse(current.targetAmountText.text)
        if (target <= 0L) return false

        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val targetDate = Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
        val days = ChronoUnit.DAYS.between(today, targetDate).coerceAtLeast(1L)
        val periods = when (current.planFrequency) {
            PlanFrequency.DAILY -> days
            PlanFrequency.WEEKLY -> ceil(days / 7.0).toLong()
            PlanFrequency.MONTHLY -> ceil(days / 30.0).toLong()
        }.coerceAtLeast(1L)
        val amount = ceil(target.toDouble() / periods.toDouble()).toLong().coerceAtLeast(1L)

        _uiState.value = current.copy(
            targetDateMillis = dateMillis,
            planAmountText = CurrencyFormatter.inputValue(amount)
        )
        return true
    }

    fun applyCalculator(frequency: PlanFrequency, amount: Long) {
        _uiState.value = _uiState.value.copy(
            planFrequency = frequency,
            planAmountText = CurrencyFormatter.inputValue(amount.coerceAtLeast(0L))
        )
    }

    fun applyPlanAmount(amount: Long) {
        applyCalculator(_uiState.value.planFrequency, amount)
    }

    fun update(transform: (GoalFormUiState) -> GoalFormUiState) {
        _uiState.value = transform(_uiState.value)
    }

    fun onImagePicked(uri: Uri, context: Context) = viewModelScope.launch {
        val path = ImageStorageManager.saveFromUri(context, uri)
        if (path != null) {
            val old = _uiState.value.imagePath
            ImageStorageManager.delete(old)
            _uiState.value = _uiState.value.copy(imagePath = path)
        }
    }

    fun removeImage() {
        ImageStorageManager.delete(_uiState.value.imagePath)
        _uiState.value = _uiState.value.copy(imagePath = null)
    }

    fun save(onDone: () -> Unit) = viewModelScope.launch {
        val s = _uiState.value
        val target = CurrencyFormatter.parse(s.targetAmountText.text)
        val plan = CurrencyFormatter.parse(s.planAmountText.text)
        if (s.title.isBlank() || target <= 0) return@launch
        _uiState.value = s.copy(saving = true)

        val existing = s.editingId?.let { container.repository.getGoalOnce(it) }
        // Celengan di arsip tidak boleh diedit lewat form (read-only).
        if (existing?.status == GoalStatus.ACHIEVED) {
            _uiState.value = s.copy(saving = false)
            onDone()
            return@launch
        }
        val goal = SavingsGoalEntity(
            id = s.editingId ?: 0,
            title = s.title.trim(),
            targetAmount = target,
            currencyCode = s.currency,
            planFrequency = s.planFrequency,
            planAmount = plan,
            targetDateMillis = s.targetDateMillis,
            imagePath = s.imagePath,
            reminderEnabled = s.reminderEnabled,
            reminderHour = s.reminderHour,
            reminderMinute = s.reminderMinute,
            reminderDaysMask = s.reminderDaysMask,
            status = existing?.status ?: GoalStatus.ONGOING,
            createdAtMillis = existing?.createdAtMillis ?: System.currentTimeMillis(),
            completedAtMillis = existing?.completedAtMillis
        )

        val savedId = if (s.editingId == null) {
            container.repository.insertGoal(goal)
        } else {
            container.repository.updateGoal(goal)
            goal.id
        }
        // Jadwalkan ulang AlarmManager memakai id final (id auto-generate untuk entri baru).
        val savedGoal = goal.copy(id = savedId)
        container.scheduler.cancel(savedId)
        if (savedGoal.reminderEnabled) container.scheduler.schedule(savedGoal)

        _uiState.value = _uiState.value.copy(saving = false)
        onDone()
    }

    fun deleteGoal(onDone: () -> Unit) = viewModelScope.launch {
        val id = _uiState.value.editingId ?: return@launch
        val existing = container.repository.getGoalOnce(id)
        existing?.let { container.repository.deleteGoal(it) }
        onDone()
    }

    companion object {
        /** Bit 0 = Senin ... bit 6 = Minggu, mengikuti konvensi layar Detail. */
        const val ALL_DAYS_MASK = 0b1111111
    }
}
