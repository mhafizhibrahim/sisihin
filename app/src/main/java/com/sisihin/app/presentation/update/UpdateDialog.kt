package com.sisihin.app.presentation.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.theme.spacing
import com.sisihin.app.data.update.UpdateChecker

/**
 * Dialog "Pembaruan Tersedia" ala Instagram: minimalis, mengikuti
 * LocalSisihinColors (Dark/Light), aksen accentBlue (#0095F6).
 */
@Composable
fun UpdateDialog(
    release: UpdateChecker.ReleaseInfo,
    currentVersion: String,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSisihinColors.current
    Dialog(onDismissRequest = onDismiss) {
        val isDarkDialog = colors.background == Color(0xFF000000)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.elevatedSurface)
                .border(
                    width = 1.dp,
                    color = if (isDarkDialog) Color(0xFF2A2A2A) else Color(0xFFE6E8EC),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(MaterialTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.SystemUpdate,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = colors.accentBlue
            )
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Text(
                text = "Pembaruan Tersedia",
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(colors.accentSoftContainer)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "v$currentVersion → v${release.version}",
                    color = colors.accentSoftContent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.height(MaterialTheme.spacing.md))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cardSurface)
                    .border(0.8.dp, colors.hairlineBorder, RoundedCornerShape(12.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.md)
            ) {
                Text(
                    text = release.changelog,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(MaterialTheme.spacing.lg))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentBlue)
                    .clickable(onClick = onUpdateClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Update Sekarang",
                    color = colors.onAccent,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
            Spacer(Modifier.height(MaterialTheme.spacing.sm))
            Text(
                text = "Nanti Saja",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onDismiss)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = colors.textMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Buka halaman unduhan update di browser; gagal senyap jika tidak ada browser. */
fun openUpdateUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        // Tidak ada aplikasi yang bisa membuka URL — abaikan.
    }
}
