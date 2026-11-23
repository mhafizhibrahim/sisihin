package com.sisihin.app.presentation.settings

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import com.sisihin.app.SisihinApplication
import com.sisihin.app.core.components.SisihinCard
import com.sisihin.app.core.security.BiometricAuthManager
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.theme.spacing
import com.sisihin.app.data.repository.BackupRestoreManager
import com.sisihin.app.data.update.UpdateChecker
import com.sisihin.app.domain.model.AppThemeMode
import com.sisihin.app.presentation.update.UpdateDialog
import com.sisihin.app.presentation.update.openUpdateUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val colors = LocalSisihinColors.current
    val container = (context.applicationContext as SisihinApplication).container
    val backupManager = remember { BackupRestoreManager(container) }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) CoroutineScope(Dispatchers.IO).launch { backupManager.export(context, uri) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) CoroutineScope(Dispatchers.IO).launch { backupManager.import(context, uri) }
    }
    var message by remember { mutableStateOf<String?>(null) }
    var delayMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var checkingUpdate by remember { mutableStateOf(false) }
    var updateRelease by remember { mutableStateOf<UpdateChecker.ReleaseInfo?>(null) }
    @Suppress("DEPRECATION")
    val versionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    message?.let {
        AlertDialog(
            onDismissRequest = { message = null },
            containerColor = colors.elevatedSurface,
            title = { Text("Informasi", color = colors.textPrimary) },
            text = { Text(it, color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text("OK", color = colors.accentSoftContent) }
            }
        )
    }

    updateRelease?.let { release ->
        UpdateDialog(
            release = release,
            currentVersion = versionName,
            onUpdateClick = {
                openUpdateUrl(context, release.downloadUrl)
                updateRelease = null
            },
            onDismiss = { updateRelease = null }
        )
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan", color = colors.textPrimary) },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.topBarBackground,
                    titleContentColor = colors.textPrimary,
                    navigationIconContentColor = colors.textPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(MaterialTheme.spacing.lg)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
        ) {
            SettingsSectionTitle("Tampilan", Icons.Outlined.Settings)
            SisihinCard {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                ) {
                    Text("Tema Aplikasi", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                    ThemeModePillRow(
                        selected = state.themeMode,
                        onSelect = { viewModel.setThemeMode(it) }
                    )
                    Text(
                        text = "Sistem mengikuti pengaturan tema perangkat.",
                        color = colors.textMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            SettingsSectionTitle("Keamanan & Privasi", Icons.Outlined.Fingerprint)
            SisihinCard {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = colors.textPrimary
                        )
                        Column(modifier = Modifier.weight(1f).padding(horizontal = MaterialTheme.spacing.md)) {
                            Text("Kunci Aplikasi (Biometrik)", color = colors.textPrimary)
                            Text(
                                if (state.biometricEnabled) {
                                    "Aktif — verifikasi saat membuka aplikasi"
                                } else {
                                    "Nonaktif secara default"
                                },
                                color = colors.textMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = state.biometricEnabled,
                            onCheckedChange = { enabled ->
                                if (!BiometricAuthManager.isAvailable(context)) {
                                    message = "Perangkat belum menyetel sidik jari/PIN layar. Buka Pengaturan Keamanan HP."
                                    return@Switch
                                }
                                if (activity == null) return@Switch
                                BiometricAuthManager.authenticate(
                                    activity = activity,
                                    title = "Verifikasi",
                                    onSuccess = { viewModel.setBiometricEnabled(enabled) },
                                    onError = { message = it }
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.onAccent,
                                checkedTrackColor = colors.accentBlue,
                                checkedBorderColor = colors.accentBlue,
                                uncheckedThumbColor = colors.textMuted,
                                uncheckedTrackColor = colors.inactiveSurface,
                                uncheckedBorderColor = colors.hairlineBorder
                            )
                        )
                    }
                    if (state.biometricEnabled) {
                        HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Kunci Otomatis", modifier = Modifier.weight(1f), color = colors.textPrimary)
                            Box {
                                TextButton(onClick = { delayMenu = true }) {
                                    Text(
                                        when (state.lockDelaySeconds) {
                                            0 -> "Segera"
                                            30 -> "30 Detik"
                                            60 -> "1 Menit"
                                            else -> "Segera"
                                        },
                                        color = colors.accentSoftContent
                                    )
                                }
                                DropdownMenu(expanded = delayMenu, onDismissRequest = { delayMenu = false }) {
                                    listOf(0, 30, 60).forEach { s ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    when (s) {
                                                        0 -> "Segera"
                                                        30 -> "30 Detik"
                                                        else -> "1 Menit"
                                                    },
                                                    color = colors.textPrimary
                                                )
                                            },
                                            onClick = { viewModel.setLockDelay(s); delayMenu = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SettingsSectionTitle("Data", Icons.Outlined.CloudUpload)
            SisihinCard {
                Column {
                    DataActionRow(
                        icon = Icons.Outlined.CloudUpload,
                        label = "Cadangkan Data",
                        sublabel = "Simpan celengan dan riwayat ke file JSON"
                    ) { exportLauncher.launch("sisihin_backup.json") }
                    HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
                    DataActionRow(
                        icon = Icons.Outlined.CloudDownload,
                        label = "Pulihkan Data",
                        sublabel = "Pulihkan dari file cadangan JSON"
                    ) { importLauncher.launch(arrayOf("application/json")) }
                }
            }

            SettingsSectionTitle("Tentang", Icons.Outlined.Info)
            SisihinCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = colors.textPrimary
                    )
                    Column(modifier = Modifier.padding(start = MaterialTheme.spacing.md)) {
                        Text("Tentang Sisihin", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Aplikasi pelacak celengan dan target tabungan.",
                            color = colors.textSecondary
                        )
                    }
                }
            }

            SisihinCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !checkingUpdate) {
                            checkingUpdate = true
                            scope.launch {
                                val release = UpdateChecker.fetchLatestRelease()
                                checkingUpdate = false
                                when {
                                    release == null ->
                                        message = "Gagal memeriksa pembaruan, cek koneksi internet"
                                    UpdateChecker.isNewerVersion(release.version, versionName) ->
                                        updateRelease = release
                                    else ->
                                        message = "Aplikasi sudah versi terbaru"
                                }
                            }
                        }
                        .padding(MaterialTheme.spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = colors.textPrimary
                    )
                    Column(modifier = Modifier.weight(1f).padding(start = MaterialTheme.spacing.md)) {
                        Text("Cek Pembaruan", color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Periksa versi terbaru Sisihin",
                            color = colors.textSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (checkingUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = colors.accentBlue,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sisihin",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textMuted
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Versi $versionName",
                    fontSize = 12.sp,
                    color = colors.textMuted.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun DataActionRow(icon: ImageVector, label: String, sublabel: String, onClick: () -> Unit) {
    val colors = LocalSisihinColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = colors.textPrimary)
        Column(modifier = Modifier.padding(start = MaterialTheme.spacing.md)) {
            Text(label, color = colors.textPrimary, style = MaterialTheme.typography.titleMedium)
            Text(sublabel, color = colors.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SettingsSectionTitle(label: String, icon: ImageVector) {
    val colors = LocalSisihinColors.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = colors.textMuted
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = MaterialTheme.spacing.sm),
            color = colors.textSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ThemeModePillRow(selected: AppThemeMode, onSelect: (AppThemeMode) -> Unit) {
    val colors = LocalSisihinColors.current
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(shape)
            .border(0.8.dp, colors.hairlineBorder, shape)
    ) {
        AppThemeMode.entries.forEachIndexed { index, mode ->
            if (index > 0) {
                VerticalDivider(
                    modifier = Modifier.fillMaxHeight().padding(vertical = 4.dp),
                    thickness = 0.8.dp,
                    color = colors.divider
                )
            }
            val active = mode == selected
            val pillSpring = spring<androidx.compose.ui.graphics.Color>(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
            val containerColor by animateColorAsState(
                targetValue = if (active) colors.accentSoftContainer else colors.cardSurface,
                animationSpec = pillSpring,
                label = "themeModePill${mode.name}"
            )
            val contentColor by animateColorAsState(
                targetValue = if (active) colors.accentSoftContent else colors.textSecondary,
                animationSpec = pillSpring,
                label = "themeModeText${mode.name}"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(containerColor)
                    .clickable { onSelect(mode) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = mode.label,
                    color = contentColor,
                    fontSize = 13.sp
                )
            }
        }
    }
}
