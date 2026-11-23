package com.sisihin.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

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
