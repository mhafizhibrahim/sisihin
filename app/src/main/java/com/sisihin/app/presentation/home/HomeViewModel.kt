package com.sisihin.app.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sisihin.app.data.AppContainer
import com.sisihin.app.data.local.GoalWithCollected
import com.sisihin.app.domain.model.GoalStatus
import com.sisihin.app.domain.model.SortDirection
import com.sisihin.app.domain.model.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

// Nama domain yang dipakai UI; model Room yang sebenarnya adalah GoalWithCollected.
typealias GoalWithProgress = GoalWithCollected

@Immutable
data class HomeUiState(
    val sortOption: SortOption = SortOption.TARGET,
    val sortDirection: SortDirection = SortDirection.ASC,
    val ongoingGoals: List<GoalWithProgress> = emptyList(),
    val achievedGoals: List<GoalWithProgress> = emptyList(),
    val totalCollected: Long = 0L,
    val activeCount: Int = 0
)

class HomeViewModel(private val container: AppContainer) : ViewModel() {
    private val sortOption = MutableStateFlow(SortOption.TARGET)
    private val sortDirection = MutableStateFlow(SortDirection.ASC)
    private val roomGoals = MutableStateFlow<List<GoalWithProgress>>(emptyList())
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        container.prefs.sortOption.onEach { v -> sortOption.value = v; recompute() }.launchIn(viewModelScope)
        container.prefs.sortDirection.onEach { v -> sortDirection.value = v; recompute() }.launchIn(viewModelScope)
        container.repository.goalsWithCollected().onEach { v -> roomGoals.value = v; recompute() }.launchIn(viewModelScope)
    }

    private fun recompute() {
        val so = sortOption.value
        val sd = sortDirection.value
        val allGoals = roomGoals.value
        val ongoingGoals = allGoals
            .filter { it.goal.status == GoalStatus.ONGOING }
            .sortedByOption(so, sd)
        val achievedGoals = allGoals
            .filter { it.goal.status == GoalStatus.ACHIEVED }
            .sortedByDescending { it.goal.completedAtMillis ?: it.goal.createdAtMillis }
        _uiState.value = HomeUiState(
            sortOption = so,
            sortDirection = sd,
            ongoingGoals = ongoingGoals,
            achievedGoals = achievedGoals,
            totalCollected = ongoingGoals.sumOf { it.collected.coerceAtLeast(0L) },
            activeCount = ongoingGoals.size
        )
    }

    private fun List<GoalWithProgress>.sortedByOption(
        so: SortOption,
        sd: SortDirection
    ): List<GoalWithProgress> {
        val sortedBase: List<GoalWithProgress> = when (so) {
            SortOption.TARGET -> sortedBy { it.goal.targetAmount }
            SortOption.PERCENT -> sortedBy { it.collected.toDouble() / it.goal.targetAmount.coerceAtLeast(1).toDouble() }
            SortOption.CREATED -> sortedBy { it.goal.createdAtMillis }
            SortOption.ESTIMATE -> sortedBy { (it.goal.targetAmount - it.collected).coerceAtLeast(0L) }
            SortOption.NAME -> sortedBy { it.goal.title }
        }
        return if (sd == SortDirection.DESC) sortedBase.reversed() else sortedBase
    }

    fun setSortOption(o: SortOption) = viewModelScope.launch { container.prefs.setSortOption(o) }
    fun setSortDirection(d: SortDirection) = viewModelScope.launch { container.prefs.setSortDirection(d) }
}
