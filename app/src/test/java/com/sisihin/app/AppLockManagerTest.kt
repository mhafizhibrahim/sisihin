package com.sisihin.app

import com.sisihin.app.core.security.AppLockManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AppLockManagerTest {
    @Test
    fun setEnabledLocks() {
        AppLockManager.setEnabled(true)
        assertEquals(true, AppLockManager.locked.value)
        AppLockManager.unlock()
        AppLockManager.setEnabled(false)
    }
}