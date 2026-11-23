package com.sisihin.app.presentation.lock

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.sisihin.app.core.theme.LocalSisihinColors

/**
 * Lapisan kunci yang hanya berupa scrim tipis di atas konten yang di-blur.
 * Tidak ada kartu, ikon, teks, maupun tombol — seluruh area layar bisa diketuk
 * untuk memunculkan ulang dialog biometrik.
 */
@Composable
fun AppLockOverlay(onUnlockClick: () -> Unit) {
    // Modifier.blur hanya bekerja di API 31+, jadi pergelapannya diperkuat di bawahnya.
    val scrimAlpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.25f else 0.72f
    val noRippleInteraction = remember { MutableInteractionSource() }
    val colors = LocalSisihinColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background.copy(alpha = scrimAlpha))
            .clickable(
                interactionSource = noRippleInteraction,
                indication = null,
                onClick = onUnlockClick
            )
    )
}
