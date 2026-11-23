package com.sisihin.app.core.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLockManager {
    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private var enabled: Boolean = false
    private var delaySeconds: Int = 0
    private var lastBackgroundAt: Long = 0L
    private var suppressNext: Boolean = false

    private val observer = object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            if (enabled) {
                lastBackgroundAt = System.currentTimeMillis()
            }
        }
        override fun onStart(owner: LifecycleOwner) {
            if (!enabled) return
            if (suppressNext) {
                suppressNext = false
                return
            }
            val now = System.currentTimeMillis()
            val elapsedSec = (now - lastBackgroundAt) / 1000L
            if (delaySeconds == 0 || elapsedSec >= delaySeconds) {
                _locked.value = true
            }
        }
    }

    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        if (!value) _locked.value = false
        if (value) _locked.value = true
    }

    fun setDelay(seconds: Int) {
        delaySeconds = seconds
    }

    fun unlock() {
        _locked.value = false
    }

    fun lockNow() {
        if (enabled) _locked.value = true
    }

    fun suppressNextLock() {
        suppressNext = true
    }

    fun isEnabled(): Boolean = enabled
}