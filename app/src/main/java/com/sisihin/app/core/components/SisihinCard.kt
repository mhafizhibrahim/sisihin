package com.sisihin.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sisihin.app.core.theme.LocalSisihinColors

/** Permukaan kartu solid dengan garis tipis, tanpa efek kaca atau gradasi. */
@Composable
fun SisihinCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    elevated: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = LocalSisihinColors.current
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (elevated) colors.elevatedSurface else colors.cardSurface,
        border = BorderStroke(0.8.dp, colors.hairlineBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box { content() }
    }
}
