package com.sisihin.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sisihin.app.core.security.AppLockManager
import com.sisihin.app.data.AppContainer
import com.sisihin.app.domain.model.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val biometricEnabled: Boolean = false,
    val lockDelaySeconds: Int = 0,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        container.prefs.biometricEnabled,
        container.prefs.lockDelaySeconds,
        container.prefs.themeMode
    ) { be, delay, themeMode ->
        SettingsUiState(be, delay, themeMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch {
        container.prefs.setBiometricEnabled(enabled)
        AppLockManager.setEnabled(enabled)
    }

    fun setLockDelay(seconds: Int) = viewModelScope.launch {
        container.prefs.setLockDelaySeconds(seconds)
        AppLockManager.setDelay(seconds)
    }

    fun setThemeMode(mode: AppThemeMode) = viewModelScope.launch {
        container.prefs.setThemeMode(mode)
    }
}
