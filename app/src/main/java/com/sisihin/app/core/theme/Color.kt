package com.sisihin.app.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Token warna Sisihin. Seluruh layar membaca dari [LocalSisihinColors],
 * sehingga mode Gelap dan Terang memakai satu jalur warna yang sama.
 */
data class SisihinColors(
    val background: Color,
    val topBarBackground: Color,
    val cardSurface: Color,
    val elevatedSurface: Color,
    val photoPlaceholder: Color,
    val hairlineBorder: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accentBlue: Color,
    val accentSoftContainer: Color,
    val accentSoftContent: Color,
    val positiveGreen: Color,
    val negativeRed: Color,
    val ringTrack: Color,
    val inactiveSurface: Color,
    val onAccent: Color = Color.White
)

val InstagramDarkColors = SisihinColors(
    background = Color(0xFF000000),
    topBarBackground = Color(0xFF000000),
    cardSurface = Color(0xFF121212),
    elevatedSurface = Color(0xFF262626),
    photoPlaceholder = Color(0xFF1C1C1E),
    hairlineBorder = Color(0xFF262626),
    divider = Color(0xFF262626),
    textPrimary = Color(0xFFF5F5F5),
    textSecondary = Color(0xFFA8A8A8),
    textMuted = Color(0xFF737373),
    accentBlue = Color(0xFF0095F6),
    accentSoftContainer = Color(0xFF18293A),
    accentSoftContent = Color(0xFF67B8FF),
    positiveGreen = Color(0xFF2ECC71),
    negativeRed = Color(0xFFED4956),
    ringTrack = Color(0xFF262626),
    inactiveSurface = Color(0xFF262626)
)

val InstagramLightColors = SisihinColors(
    background = Color(0xFFFFFFFF),
    topBarBackground = Color(0xFFFFFFFF),
    cardSurface = Color(0xFFFAFAFA),
    elevatedSurface = Color(0xFFFFFFFF),
    photoPlaceholder = Color(0xFFEFEFEF),
    hairlineBorder = Color(0xFFDBDBDB),
    divider = Color(0xFFDBDBDB),
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF737373),
    textMuted = Color(0xFF999999),
    accentBlue = Color(0xFF0095F6),
    accentSoftContainer = Color(0xFFE7F3FF),
    accentSoftContent = Color(0xFF0095F6),
    positiveGreen = Color(0xFF16A34A),
    negativeRed = Color(0xFFED4956),
    ringTrack = Color(0xFFEFEFEF),
    inactiveSurface = Color(0xFFEFEFEF)
)

val LocalSisihinColors = compositionLocalOf { InstagramDarkColors }

val SisihinDarkColors = darkColorScheme(
    primary = InstagramDarkColors.accentBlue,
    onPrimary = InstagramDarkColors.onAccent,
    primaryContainer = InstagramDarkColors.accentSoftContainer,
    onPrimaryContainer = InstagramDarkColors.accentSoftContent,
    secondary = InstagramDarkColors.textSecondary,
    onSecondary = InstagramDarkColors.background,
    tertiary = InstagramDarkColors.positiveGreen,
    onTertiary = InstagramDarkColors.background,
    background = InstagramDarkColors.background,
    onBackground = InstagramDarkColors.textPrimary,
    surface = InstagramDarkColors.cardSurface,
    onSurface = InstagramDarkColors.textPrimary,
    surfaceVariant = InstagramDarkColors.elevatedSurface,
    onSurfaceVariant = InstagramDarkColors.textSecondary,
    surfaceContainerLowest = InstagramDarkColors.background,
    surfaceContainerLow = InstagramDarkColors.cardSurface,
    surfaceContainer = InstagramDarkColors.cardSurface,
    surfaceContainerHigh = InstagramDarkColors.elevatedSurface,
    surfaceContainerHighest = InstagramDarkColors.elevatedSurface,
    outline = InstagramDarkColors.hairlineBorder,
    outlineVariant = InstagramDarkColors.divider,
    error = InstagramDarkColors.negativeRed,
    onError = InstagramDarkColors.onAccent,
    scrim = Color.Black
)

val SisihinLightColors = lightColorScheme(
    primary = InstagramLightColors.accentBlue,
    onPrimary = InstagramLightColors.onAccent,
    primaryContainer = InstagramLightColors.accentSoftContainer,
    onPrimaryContainer = InstagramLightColors.accentSoftContent,
    secondary = InstagramLightColors.textSecondary,
    onSecondary = InstagramLightColors.background,
    tertiary = InstagramLightColors.positiveGreen,
    onTertiary = InstagramLightColors.background,
    background = InstagramLightColors.background,
    onBackground = InstagramLightColors.textPrimary,
    surface = InstagramLightColors.cardSurface,
    onSurface = InstagramLightColors.textPrimary,
    surfaceVariant = InstagramLightColors.elevatedSurface,
    onSurfaceVariant = InstagramLightColors.textSecondary,
    surfaceContainerLowest = InstagramLightColors.background,
    surfaceContainerLow = InstagramLightColors.cardSurface,
    surfaceContainer = InstagramLightColors.cardSurface,
    surfaceContainerHigh = InstagramLightColors.elevatedSurface,
    surfaceContainerHighest = InstagramLightColors.elevatedSurface,
    outline = InstagramLightColors.hairlineBorder,
    outlineVariant = InstagramLightColors.divider,
    error = InstagramLightColors.negativeRed,
    onError = InstagramLightColors.onAccent,
    scrim = Color.Black
)
