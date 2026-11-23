package com.sisihin.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sisihin.app.core.theme.LocalSisihinColors
import kotlin.math.absoluteValue

private val GoalAccents = listOf(
    Color(0xFF0095F6), // Instagram Blue
    Color(0xFF10B981), // Emerald
    Color(0xFF8B5CF6), // Violet
    Color(0xFFF59E0B), // Amber
    Color(0xFFF43F5E), // Rose
    Color(0xFF06B6D4)  // Cyan
)

private val ThumbnailShape: Shape = RoundedCornerShape(12.dp)

/** Warna aksen deterministik: judul yang sama selalu mendapat warna yang sama. */
private fun accentFor(title: String): Color {
    // toLong() menghindari overflow Int.MIN_VALUE saat di-absoluteValue.
    val index = (title.trim().lowercase().hashCode().toLong().absoluteValue % GoalAccents.size).toInt()
    return GoalAccents[index]
}

/** Inisial tabungan: dua kata pertama atau maksimal dua huruf pertama. */
fun extractGoalInitials(title: String): String {
    val words = title.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val initials = when {
        words.isEmpty() -> "S"
        words.size >= 2 -> words[0].take(1) + words[1].take(1)
        else -> words[0].take(2)
    }
    return initials.uppercase()
}

/**
 * Placeholder celengan tanpa foto. Monogram inisial + watermark Savings menggantikan
 * ikon lanskap, dengan warna aksen yang konsisten per judul tabungan.
 */
@Composable
fun GoalPlaceholderVisual(
    title: String,
    isBanner: Boolean,
    modifier: Modifier = Modifier,
    shape: Shape = ThumbnailShape
) {
    val colors = LocalSisihinColors.current
    // Diturunkan dari tema yang sedang dipakai, bukan setelan sistem, agar mengikuti
    // mode DARK / LIGHT / SYSTEM secara eksak.
    val isDarkTheme = colors.background.luminance() < 0.5f
    val accent = accentFor(title)
    val initials = extractGoalInitials(title)
    val containerColor = if (isDarkTheme) {
        accent.copy(alpha = 0.14f)
    } else {
        accent.copy(alpha = 0.10f)
    }
    val borderColor = if (isDarkTheme) {
        accent.copy(alpha = 0.24f)
    } else {
        accent.copy(alpha = 0.22f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = shape),
        contentAlignment = Alignment.Center
    ) {
        if (isBanner) {
            Icon(
                imageVector = Icons.Outlined.Savings,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 18.dp, y = 12.dp)
                    .size(110.dp),
                tint = accent.copy(alpha = 0.10f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f))
                        .border(width = 1.dp, color = accent.copy(alpha = 0.35f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = accent,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Belum ada foto",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
        } else {
            Icon(
                imageVector = Icons.Outlined.Savings,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(34.dp),
                tint = accent.copy(alpha = 0.14f)
            )
            Text(
                text = initials,
                color = accent,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
