package com.sisihin.app.core.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Token tema dipasang seketika (satu rekomposisi), tanpa layer penutup atau animasi
 * warna per frame. Ini menjaga FPS tetap stabil dan mencegah UI tertutup layer
 * hitam/putih saat ganti tema.
 */
@Composable
fun SisihinTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) InstagramDarkColors else InstagramLightColors
    val colorScheme = if (isDarkTheme) SisihinDarkColors else SisihinLightColors
    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalSisihinColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SisihinTypography,
            shapes = SisihinShapes,
            content = content
        )
    }
}

/**
 * Animasi warna untuk kontrol interaktif (pill, chip, segment) yang sadar tema:
 * - Ganti palet tema  -> snap(), warna melompat seketika di frame yang sama
 *   dengan seluruh UI lain (tanpa belang/tertinggal).
 * - Ganti seleksi di palet yang sama -> tetap memakai spring pemanggil.
 *
 * Jangan pakai animateColorAsState biasa untuk warna yang berasal dari
 * LocalSisihinColors: spring-nya meng-interpolasi warna tema lama ke baru
 * selama ratusan ms sementara komponen lain sudah berganti instan.
 */
@Composable
fun animateThemeColor(
    targetValue: Color,
    animationSpec: AnimationSpec<Color>,
    label: String = "themeColor"
): State<Color> {
    val palette = LocalSisihinColors.current
    var lastPalette by remember { mutableStateOf(palette) }
    val paletteChanged = lastPalette !== palette
    SideEffect { lastPalette = palette }
    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = if (paletteChanged) snap() else animationSpec,
        label = label
    )
}
