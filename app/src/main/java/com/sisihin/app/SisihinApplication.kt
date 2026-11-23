package com.sisihin.app

import android.app.Application
import com.sisihin.app.core.security.AppLockManager
import com.sisihin.app.data.AppContainer

class SisihinApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        AppLockManager.init()
    }
}