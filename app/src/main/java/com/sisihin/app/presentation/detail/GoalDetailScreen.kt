package com.sisihin.app.presentation.detail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.ui.GoalPlaceholderVisual
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.core.util.DateFormatter
import com.sisihin.app.core.util.SavingsCalculator
import com.sisihin.app.data.local.SavingsGoalEntity
import com.sisihin.app.data.local.SavingsTransactionEntity
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.PlanFrequency
import com.sisihin.app.domain.model.TransactionType
import java.io.File
import kotlinx.coroutines.launch

private val PhotoBlockShape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 20.dp, bottomStart = 6.dp, bottomEnd = 6.dp
)
private val MiddleBlockShape = RoundedCornerShape(6.dp)
private val BottomBlockShape = RoundedCornerShape(
    topStart = 6.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp
)
private val CirclePillShape = RoundedCornerShape(999.dp)
private val RingSpring = spring<Float>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)
private val DayNames = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu", "Minggu")

@Composable
private fun Modifier.detailBlock(shape: Shape): Modifier {
    val colors = LocalSisihinColors.current
    return this
        .fillMaxWidth()
        .background(colors.cardSurface, shape)
        .border(width = 0.8.dp, color = colors.hairlineBorder, shape = shape)
}

@Composable
fun GoalDetailScreen(
    viewModel: GoalDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = LocalSisihinColors.current
    val goal = uiState.goal
    val archived = uiState.isArchived
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var recordInitialType by remember { mutableStateOf(TransactionType.DEPOSIT) }
    var editingTx by remember { mutableStateOf<SavingsTransactionEntity?>(null) }
    var selectedTx by remember { mutableStateOf<SavingsTransactionEntity?>(null) }
    var showPhotoPreview by remember { mutableStateOf(false) }

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    if (goal == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Column {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Kembali",
                        modifier = Modifier.size(20.dp),
                        tint = colors.textPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text("Memuat celengan...", color = colors.textSecondary, fontSize = 14.sp)
            }
        }
        return
    }

    val photoFile = goal.imagePath?.takeIf { it.isNotBlank() }?.let(::File)?.takeIf { it.exists() }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = colors.elevatedSurface,
            tonalElevation = 0.dp,
            title = { Text("Hapus celengan ini?", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Riwayat transaksi dan foto celengan juga akan dihapus. Tindakan ini tidak dapat dibatalkan.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteGoal(onBack)
                }) { Text("Hapus", color = colors.negativeRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal", color = colors.textSecondary) }
            }
        )
    }

    if (showRecordDialog && !archived) {
        RecordTransactionDialog(
            initialType = editingTx?.type ?: recordInitialType,
            initialAmount = editingTx?.amount ?: 0L,
            initialNote = editingTx?.note.orEmpty(),
            collected = uiState.collected,
            targetAmount = goal.targetAmount,
            planAmount = goal.planAmount,
            currencyCode = goal.currencyCode,
            onDismiss = { showRecordDialog = false },
            onSave = { type, amount, note ->
                val updated = (editingTx ?: SavingsTransactionEntity(
                    goalId = goal.id,
                    type = TransactionType.DEPOSIT,
                    amount = 0L
                )).copy(
                    goalId = goal.id,
                    type = type,
                    amount = amount,
                    note = note
                )
                if (editingTx == null) {
                    viewModel.addTransaction(updated)
                } else {
                    viewModel.updateTransaction(updated)
                }
                showRecordDialog = false
                editingTx = null
            }
        )
    }

    if (!archived) selectedTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { selectedTx = null },
            containerColor = colors.elevatedSurface,
            tonalElevation = 0.dp,
            title = { Text("Ubah transaksi", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(DateFormatter.dateTime(tx.timestampMillis), color = colors.textSecondary, fontSize = 13.sp)
                    Text(
                        text = (if (tx.type == TransactionType.DEPOSIT) "+ " else "- ") +
                            formatTransactionAmount(tx.amount, goal.currencyCode),
                        color = if (tx.type == TransactionType.DEPOSIT) colors.positiveGreen else colors.negativeRed,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (tx.note.isNotBlank()) {
                        Text(tx.note, color = colors.textSecondary, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editingTx = tx
                    selectedTx = null
                    showRecordDialog = true
                }) { Text("Edit", color = colors.accentSoftContent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(tx)
                    selectedTx = null
                }) { Text("Hapus", color = colors.negativeRed) }
            }
        )
    }

    if (showPhotoPreview && photoFile != null) {
        Dialog(
            onDismissRequest = { showPhotoPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
                val previewRequest = remember(photoFile, context) {
                    ImageRequest.Builder(context)
                        .data(photoFile)
                        .size(800, 500)
                        .crossfade(150)
                        .build()
                }
                AsyncImage(
                    model = previewRequest,
                    contentDescription = "Foto ${goal.title}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { showPhotoPreview = false },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Tutup pratinjau",
                        modifier = Modifier.size(20.dp),
                        tint = colors.textPrimary
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.topBarBackground)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Kembali",
                            modifier = Modifier.size(20.dp),
                            tint = colors.textPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!archived) {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "Edit celengan",
                                    modifier = Modifier.size(22.dp),
                                    tint = colors.accentBlue
                                )
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Hapus celengan",
                                modifier = Modifier.size(22.dp),
                                tint = colors.accentBlue
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 14.dp)
                ) {
                    Text(
                        text = goal.title,
                        color = colors.textPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colors.background),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            item {
                PhotoBlock(
                    photoFile = photoFile,
                    title = goal.title,
                    onClick = { if (photoFile != null) showPhotoPreview = true }
                )
            }
            item {
                TargetSummaryBlock(goal = goal, collected = uiState.collected)
            }
            item {
                BalanceBlock(goal = goal, collected = uiState.collected)
            }
            item {
                if (archived) {
                    CelebrationBanner(
                        days = SavingsCalculator.daysTaken(
                            goal.createdAtMillis,
                            goal.completedAtMillis ?: System.currentTimeMillis()
                        )
                    )
                } else {
                    ActionButtonsBlock(
                        amounts = uiState.suggestedAmounts,
                        canWithdraw = uiState.collected > 0,
                        onDeposit = {
                            editingTx = null
                            recordInitialType = TransactionType.DEPOSIT
                            showRecordDialog = true
                        },
                        onWithdraw = {
                            editingTx = null
                            recordInitialType = TransactionType.WITHDRAW
                            showRecordDialog = true
                        },
                        onPickAmount = { amount ->
                            scope.launch {
                                val txId = viewModel.depositAmount(amount)
                                if (txId != null) {
                                    val label = "+${CurrencyFormatter.formatInput(amount.toString())}"
                                    val result = snackbarHostState.showSnackbar(
                                        message = "$label ditambahkan",
                                        actionLabel = "Urungkan"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) viewModel.undoDeposit(txId)
                                }
                            }
                        }
                    )
                }
            }
            if (!archived) {
                item {
                    ReminderBlock(
                        goal = goal,
                        onToggle = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.updateReminder(enabled = enabled)
                        }
                    )
                }
            }
            item {
                HistoryBlock(
                    goal = goal,
                    collected = uiState.collected,
                    transactions = uiState.transactions,
                    archived = archived,
                    onSelect = { tx -> selectedTx = tx }
                )
            }
        }
    }
}

@Composable
private fun PhotoBlock(photoFile: File?, title: String, onClick: () -> Unit) {
    val context = LocalContext.current
    val colors = LocalSisihinColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(165.dp)
            .clip(PhotoBlockShape)
            .then(
                if (photoFile != null) {
                    Modifier
                        .background(colors.photoPlaceholder)
                        .border(0.8.dp, colors.hairlineBorder, PhotoBlockShape)
                } else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (photoFile != null) {
            val imageRequest = remember(photoFile, context) {
                ImageRequest.Builder(context)
                    .data(photoFile)
                    .size(800, 500)
                    .crossfade(150)
                    .build()
            }
            AsyncImage(
                model = imageRequest,
                contentDescription = "Foto $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            GoalPlaceholderVisual(
                title = title,
                isBanner = true,
                modifier = Modifier.fillMaxSize(),
                shape = PhotoBlockShape
            )
        }
    }
}

@Composable
private fun TargetSummaryBlock(goal: SavingsGoalEntity, collected: Long) {
    val colors = LocalSisihinColors.current
    Column(
        modifier = Modifier
            .detailBlock(MiddleBlockShape)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatMoney(goal.targetAmount, goal.currencyCode),
                    color = colors.textPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Normal,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${formatMoney(goal.planAmount, goal.currencyCode)} " +
                        frequencyLabel(goal.planFrequency),
                    color = colors.textSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(Modifier.width(12.dp))
            ProgressRing(percent = SavingsCalculator.percent(goal.targetAmount, collected).toInt())
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
        Spacer(Modifier.height(14.dp))
        SummaryRow("Tanggal Dibuat", DateFormatter.date(goal.createdAtMillis))
        Spacer(Modifier.height(10.dp))
        val remaining = SavingsCalculator.remaining(goal.targetAmount, collected)
        SummaryRow(
            label = "Estimasi",
            value = if (remaining == 0L) {
                "Target Tercapai"
            } else {
                SavingsCalculator.estimateText(remaining, goal.planAmount, goal.planFrequency)
            }
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    val colors = LocalSisihinColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = colors.textSecondary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text(value, color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProgressRing(percent: Int) {
    val colors = LocalSisihinColors.current
    val animatedSweep by animateFloatAsState(
        targetValue = 360f * percent.coerceIn(0, 100) / 100f,
        animationSpec = RingSpring,
        label = "detailProgressRing"
    )
    Box(modifier = Modifier.size(58.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 5.dp.toPx()
            drawArc(
                color = colors.ringTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )
            drawArc(
                color = colors.accentBlue,
                startAngle = -90f,
                sweepAngle = animatedSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$percent%",
            color = colors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ReminderBlock(goal: SavingsGoalEntity, onToggle: (Boolean) -> Unit) {
    val colors = LocalSisihinColors.current
    Row(
        modifier = Modifier
            .detailBlock(MiddleBlockShape)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CirclePillShape)
                .background(if (goal.reminderEnabled) colors.accentBlue else colors.inactiveSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (goal.reminderEnabled) {
                    Icons.Outlined.NotificationsActive
                } else {
                    Icons.Outlined.NotificationsOff
                },
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = if (goal.reminderEnabled) colors.onAccent else colors.textMuted
            )
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(
                text = DateFormatter.time(goal.reminderHour, goal.reminderMinute),
                color = if (goal.reminderEnabled) colors.textPrimary else colors.textMuted,
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                style = TextStyle(fontFeatureSettings = "tnum")
            )
            Text(
                text = daysLabel(goal.reminderDaysMask),
                color = if (goal.reminderEnabled) colors.textSecondary else colors.textMuted,
                fontSize = 13.sp
            )
        }
        Switch(
            checked = goal.reminderEnabled,
            onCheckedChange = onToggle,
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
}

@Composable
private fun BalanceBlock(goal: SavingsGoalEntity, collected: Long) {
    val colors = LocalSisihinColors.current
    val remaining = SavingsCalculator.remaining(goal.targetAmount, collected)
    Column(
        modifier = Modifier
            .detailBlock(MiddleBlockShape)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text("SALDO", color = colors.textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BalanceCell("Terkumpul", formatMoney(collected, goal.currencyCode), colors.positiveGreen)
            VerticalDivider(
                modifier = Modifier.height(38.dp),
                thickness = 0.8.dp,
                color = colors.divider
            )
            BalanceCell("Kekurangan", formatMoney(remaining, goal.currencyCode), colors.negativeRed)
        }
    }
}

@Composable
private fun ActionButtonsBlock(
    amounts: List<Long>,
    canWithdraw: Boolean,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onPickAmount: (Long) -> Unit
) {
    val colors = LocalSisihinColors.current
    Column(
        modifier = Modifier
            .detailBlock(MiddleBlockShape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
                onClick = onDeposit,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = CirclePillShape,
                color = colors.accentBlue
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("+ Tambah", color = colors.onAccent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Surface(
                onClick = onWithdraw,
                enabled = canWithdraw,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .alpha(if (canWithdraw) 1f else 0.4f),
                shape = CirclePillShape,
                color = colors.cardSurface,
                border = BorderStroke(0.8.dp, colors.hairlineBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("- Kurangi", color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        NominalChipsBlock(amounts = amounts, onPick = onPickAmount)
    }
}

@Composable
private fun NominalChipsBlock(amounts: List<Long>, onPick: (Long) -> Unit) {
    val colors = LocalSisihinColors.current
    if (amounts.isEmpty()) {
        Text(
            text = "Atur nominal pengisian untuk memilih setoran cepat.",
            color = colors.textMuted,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        amounts.forEach { amount ->
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clickable { onPick(amount) },
                shape = CirclePillShape,
                color = colors.accentSoftContainer,
                border = BorderStroke(0.8.dp, colors.hairlineBorder)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = CurrencyFormatter.formatInput(amount.toString()),
                        color = colors.accentSoftContent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CelebrationBanner(days: Long) {
    val colors = LocalSisihinColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.accentSoftContainer, MiddleBlockShape)
            .border(0.8.dp, colors.accentBlue, MiddleBlockShape)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            tint = colors.accentSoftContent
        )
        Spacer(Modifier.height(10.dp))
        Text("100% Tercapai", color = colors.accentSoftContent, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Target tercapai dalam $days hari. Celengan ini sudah diarsipkan dan tidak dapat diubah.",
            color = colors.textSecondary,
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun HistoryBlock(
    goal: SavingsGoalEntity,
    collected: Long,
    transactions: List<SavingsTransactionEntity>,
    archived: Boolean,
    onSelect: (SavingsTransactionEntity) -> Unit
) {
    val colors = LocalSisihinColors.current
    Column(
        modifier = Modifier
            .detailBlock(BottomBlockShape)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Text(
            text = if (archived) "RIWAYAT SETORAN HARIAN" else "RIWAYAT TABUNGAN",
            color = colors.textMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Tidak Ada Riwayat Tabungan", color = colors.textSecondary, fontSize = 14.sp)
            }
        } else if (archived) {
            val grouped = LinkedHashMap<String, MutableList<SavingsTransactionEntity>>()
            transactions.forEach { tx ->
                grouped.getOrPut(DateFormatter.date(tx.timestampMillis)) { mutableListOf() }.add(tx)
            }
            grouped.forEach { (day, txs) ->
                val dailyTotal = txs.sumOf { if (it.type == TransactionType.DEPOSIT) it.amount else -it.amount }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(day, color = colors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = "+${formatTransactionAmount(dailyTotal.coerceAtLeast(0L), goal.currencyCode)}",
                        color = colors.positiveGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                }
                txs.forEach { tx ->
                    TransactionRow(tx = tx, currency = goal.currencyCode, onClick = null)
                }
                HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
            }
        } else {
            transactions.forEachIndexed { index, tx ->
                TransactionRow(
                    tx = tx,
                    currency = goal.currencyCode,
                    onClick = { onSelect(tx) }
                )
                if (index != transactions.lastIndex) {
                    HorizontalDivider(thickness = 0.8.dp, color = colors.divider)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun RowScope.BalanceCell(label: String, amount: String, color: androidx.compose.ui.graphics.Color) {
    val colors = LocalSisihinColors.current
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))
        Text(amount, color = color, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TransactionRow(
    tx: SavingsTransactionEntity,
    currency: CurrencyCode,
    onClick: (() -> Unit)?
) {
    val colors = LocalSisihinColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(DateFormatter.dateTime(tx.timestampMillis), color = colors.textPrimary, fontSize = 14.sp)
            if (tx.note.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(tx.note, color = colors.textSecondary, fontSize = 12.sp, maxLines = 2)
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = (if (tx.type == TransactionType.DEPOSIT) "+ " else "- ") +
                formatTransactionAmount(tx.amount, currency),
            color = if (tx.type == TransactionType.DEPOSIT) colors.positiveGreen else colors.negativeRed,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Urutan enum: harian, mingguan, bulanan.
private fun frequencyLabel(frequency: PlanFrequency): String = when (frequency.ordinal) {
    0 -> "Perhari"
    1 -> "Perminggu"
    else -> "Perbulan"
}

private fun daysLabel(mask: Int): String {
    if (mask == 0) return "Setiap hari"
    return DayNames.filterIndexed { index, _ -> mask and (1 shl index) != 0 }
        .joinToString(", ")
}

/** Tanpa spasi setelah simbol: Rp2.000, bukan Rp 2.000. */
private fun formatMoney(amount: Long, currency: CurrencyCode): String =
    CurrencyFormatter.format(amount, currency).replace(Regex("\\s+"), "")

/** Nominal transaksi tanpa simbol mata uang: 10.000. */
private fun formatTransactionAmount(amount: Long, currency: CurrencyCode): String =
    formatMoney(amount, currency).removePrefix(currency.symbol)
