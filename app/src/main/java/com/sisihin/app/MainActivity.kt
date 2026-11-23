package com.sisihin.app

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sisihin.app.core.security.AppLockManager
import com.sisihin.app.core.security.BiometricAuthManager
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.theme.SisihinTheme
import com.sisihin.app.domain.model.AppThemeMode
import com.sisihin.app.data.update.UpdateChecker
import com.sisihin.app.presentation.common.SisihinNavHost
import com.sisihin.app.presentation.lock.AppLockOverlay
import com.sisihin.app.presentation.update.UpdateDialog
import com.sisihin.app.presentation.update.openUpdateUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class MainActivity : FragmentActivity() {

    companion object {
        // Cek update otomatis cukup sekali per proses, bukan per recreate activity.
        private var autoUpdateChecked = false
    }

    private var isPromptActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(TRANSPARENT)
        )
        setContent {
            val container = (application as SisihinApplication).container
            val biometricEnabled by container.prefs.biometricEnabled.collectAsState(initial = false)
            val lockDelay by container.prefs.lockDelaySeconds.collectAsState(initial = 0)
            val themeMode by container.prefs.themeMode.collectAsState(initial = AppThemeMode.SYSTEM)
            val systemDarkTheme = isSystemInDarkTheme()
            val isDarkTheme = when (themeMode) {
                AppThemeMode.SYSTEM -> systemDarkTheme
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
            }

            // Cukup flip flag ikon; enableEdgeToEdge dipanggil sekali di onCreate.
            DisposableEffect(isDarkTheme) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller?.isAppearanceLightStatusBars = !isDarkTheme
                controller?.isAppearanceLightNavigationBars = !isDarkTheme
                onDispose { }
            }

            LaunchedEffect(biometricEnabled, lockDelay) {
                AppLockManager.setEnabled(biometricEnabled)
                AppLockManager.setDelay(lockDelay)
            }

            val locked by AppLockManager.locked.collectAsState()
            val activityLifecycle = this@MainActivity.lifecycle
            var isResumed by remember {
                mutableStateOf(activityLifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
            }
            DisposableEffect(activityLifecycle) {
                val observer = LifecycleEventObserver { _, _ ->
                    isResumed = activityLifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
                }
                activityLifecycle.addObserver(observer)
                onDispose { activityLifecycle.removeObserver(observer) }
            }

            LaunchedEffect(locked, isResumed) {
                if (locked && isResumed) {
                    // Beri waktu jendela Android siap menerima dialog pada cold start.
                    delay(120)
                    requestBiometric()
                }
            }

            // Cek update otomatis: sekali per proses, di background, tanpa
            // memblokir splash/biometrik. Gagal (offline/404/rate-limit) senyap.
            val currentVersion = remember { UpdateChecker.currentVersion(this@MainActivity) }
            var pendingUpdate by remember { mutableStateOf<UpdateChecker.ReleaseInfo?>(null) }
            LaunchedEffect(Unit) {
                snapshotFlow { locked }.first { !it }
                if (autoUpdateChecked) return@LaunchedEffect
                autoUpdateChecked = true
                delay(1000)
                val release = UpdateChecker.fetchLatestRelease()
                if (release != null && UpdateChecker.isNewerVersion(release.version, currentVersion)) {
                    pendingUpdate = release
                }
            }

            SisihinTheme(isDarkTheme = isDarkTheme) {
                val colors = LocalSisihinColors.current
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                        .then(
                            if (locked) {
                                Modifier.blur(
                                    radius = 8.dp,
                                    edgeTreatment = BlurredEdgeTreatment.Rectangle
                                )
                            } else {
                                Modifier
                            }
                        )
                ) {
                    SisihinNavHost()
                }

                if (locked) {
                    AppLockOverlay(onUnlockClick = { requestBiometric() })
                }

                if (!locked) {
                    pendingUpdate?.let { release ->
                        UpdateDialog(
                            release = release,
                            currentVersion = currentVersion,
                            onUpdateClick = {
                                openUpdateUrl(this@MainActivity, release.downloadUrl)
                                pendingUpdate = null
                            },
                            onDismiss = { pendingUpdate = null }
                        )
                    }
                }

                BackHandler(enabled = locked) {
                    moveTaskToBack(true)
                }
            }
        }
    }

    private fun requestBiometric() {
        if (isPromptActive) return
        isPromptActive = true
        try {
            BiometricAuthManager.authenticate(
                activity = this,
                onSucceeded = {
                    isPromptActive = false
                    AppLockManager.unlock()
                },
                onError = { isPromptActive = false },
                onCancelled = { isPromptActive = false }
            )
        } catch (e: Exception) {
            // Window belum siap atau biometrik gagal dibuka: tetap tahan di layar blur.
            // Pengguna bisa mengetuk layar untuk mencoba lagi.
            isPromptActive = false
        }
    }
}
