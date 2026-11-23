package com.sisihin.app.presentation.form

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ShortText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sisihin.app.R
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.theme.animateThemeColor
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.PlanFrequency
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private val FieldShape = RoundedCornerShape(8.dp)
private val DropdownShape = RoundedCornerShape(14.dp)
private val TrackShape = RoundedCornerShape(14.dp)
private val TrackItemShape = RoundedCornerShape(10.dp)
private val PhotoCorner = 16.dp
private val PhotoShape = RoundedCornerShape(PhotoCorner)
private val PillShape = RoundedCornerShape(50)
private val ChipShape = RoundedCornerShape(8.dp)
private val FlagShape = RoundedCornerShape(3.dp)
private val LinkShape = RoundedCornerShape(8.dp)
private val TabularStyle = TextStyle(fontFeatureSettings = "tnum")
private val DisplayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))
private val MicroSpring = spring<Color>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

@Composable
private fun formFieldColors() = with(LocalSisihinColors.current) {
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        disabledTextColor = textMuted,
        focusedLabelColor = textSecondary,
        unfocusedLabelColor = textSecondary,
        disabledLabelColor = textMuted,
        focusedBorderColor = hairlineBorder,
        unfocusedBorderColor = hairlineBorder,
        disabledBorderColor = hairlineBorder,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        cursorColor = accentBlue,
        focusedTrailingIconColor = textSecondary,
        unfocusedTrailingIconColor = textSecondary
    )
}

@Composable
private fun formSwitchColors() = with(LocalSisihinColors.current) {
    SwitchDefaults.colors(
        checkedThumbColor = onAccent,
        checkedTrackColor = accentBlue,
        uncheckedThumbColor = textMuted,
        uncheckedTrackColor = inactiveSurface,
        checkedBorderColor = accentBlue,
        uncheckedBorderColor = hairlineBorder
    )
}

private val DayLabels = listOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")

/** Urutan chip: Minggu, Senin, ... Sabtu. Bit mengikuti konvensi layar Detail (bit 0 = Senin). */
private fun dayBit(displayIndex: Int): Int = if (displayIndex == 0) 1 shl 6 else 1 shl (displayIndex - 1)

/** Label lokal tanpa menyebut konstanta enum mentah di UI. */
private fun PlanFrequency.label(): String = when (ordinal) {
    0 -> "Harian"
    1 -> "Mingguan"
    else -> "Bulanan"
}

/** Kata periode untuk kalimat helper, mis. "Disisihkan setiap minggu". */
private fun PlanFrequency.periodWord(): String = when (ordinal) {
    0 -> "hari"
    1 -> "minggu"
    else -> "bulan"
}

private fun flagRes(currency: CurrencyCode): Int = when (currency) {
    CurrencyCode.IDR -> R.drawable.flag_id
    CurrencyCode.USD -> R.drawable.flag_us
    CurrencyCode.JPY -> R.drawable.flag_jp
    CurrencyCode.EUR -> R.drawable.flag_eu
    CurrencyCode.SGD -> R.drawable.flag_sg
    CurrencyCode.MYR -> R.drawable.flag_my
    CurrencyCode.KRW -> R.drawable.flag_kr
    CurrencyCode.GBP -> R.drawable.flag_gb
    CurrencyCode.AUD -> R.drawable.flag_au
    CurrencyCode.SAR -> R.drawable.flag_sa
}

private fun CurrencyCode.formLabel(): String = label.replace("(", "( ").replace(")", " )")

private fun formatTargetDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate().format(DisplayDateFormatter)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GoalFormScreen(
    viewModel: GoalFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = LocalSisihinColors.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val iconColumnWidth = 44.dp
    val fieldColors = formFieldColors()
    val switchColors = formSwitchColors()

    var currencySheetOpen by remember { mutableStateOf(false) }
    var calculatorSheetOpen by remember { mutableStateOf(false) }
    var targetDatePickerOpen by remember { mutableStateOf(false) }
    var reminderTimePickerOpen by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        uri?.let { viewModel.onImagePicked(it, context) }
    }
    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun launchPhotoPicker() {
        photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    if (currencySheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { currencySheetOpen = false },
            containerColor = colors.elevatedSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text("Pilih Mata Uang", color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                CurrencyCode.entries.forEach { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(ChipShape)
                            .clickable {
                                viewModel.setCurrency(currency)
                                currencySheetOpen = false
                            }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CurrencyFlagIcon(currency)
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                currency.formLabel(),
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Text(
                                "${currency.name} • ${currency.symbol}",
                                color = colors.textSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        if (currency == state.currency) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Dipilih",
                                modifier = Modifier.size(18.dp),
                                tint = colors.accentBlue
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (targetDatePickerOpen) {
        val tomorrowMillis = remember {
            LocalDate.now(ZoneId.systemDefault()).plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val selectableDates = remember(tomorrowMillis) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= tomorrowMillis
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tomorrowMillis,
            initialDisplayedMonthMillis = tomorrowMillis,
            selectableDates = selectableDates
        )
        AlertDialog(
            onDismissRequest = { targetDatePickerOpen = false },
            containerColor = colors.elevatedSurface,
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null && !viewModel.setTargetDateFromPicker(millis)) {
                        showMessage("Isi Target Tabungan terlebih dahulu")
                    }
                    targetDatePickerOpen = false
                }) { Text("OK", color = colors.accentSoftContent) }
            },
            dismissButton = {
                TextButton(onClick = { targetDatePickerOpen = false }) {
                    Text("Batal", color = colors.textSecondary)
                }
            },
            text = { DatePicker(state = datePickerState) }
        )
    }

    if (reminderTimePickerOpen) {
        val timePickerState = rememberTimePickerState(
            initialHour = state.reminderHour,
            initialMinute = state.reminderMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { reminderTimePickerOpen = false },
            containerColor = colors.elevatedSurface,
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminderTime(timePickerState.hour, timePickerState.minute)
                    reminderTimePickerOpen = false
                }) { Text("OK", color = colors.accentSoftContent) }
            },
            dismissButton = {
                TextButton(onClick = { reminderTimePickerOpen = false }) {
                    Text("Batal", color = colors.textSecondary)
                }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    if (calculatorSheetOpen) {
        TargetCalculatorSheet(
            initialTargetText = state.targetAmountText.text,
            initialFrequency = state.planFrequency,
            currencyCode = state.currency,
            onApply = { frequency, amount, target ->
                viewModel.applyCalculator(frequency, amount)
                // Bila target diisi dari dalam kalkulator (form masih kosong), ikut terapkan.
                if (target > 0L && CurrencyFormatter.parse(state.targetAmountText.text) <= 0L) {
                    viewModel.onTargetAmountChange(CurrencyFormatter.inputValue(target))
                }
                calculatorSheetOpen = false
            },
            onDismiss = { calculatorSheetOpen = false }
        )
    }

    Scaffold(
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.topBarBackground)
                    .statusBarsPadding()
                    .height(60.dp)
                    .padding(horizontal = 12.dp),
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
                Button(
                    onClick = { viewModel.save(onSaved) },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentBlue,
                        contentColor = colors.onAccent
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text(
                        "Simpan",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onAccent
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // A. Kotak unggah foto — sejajar dengan kolom input kiri.
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(iconColumnWidth))
                val imagePath = state.imagePath
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(190.dp)
                        .clip(PhotoShape)
                        .background(colors.cardSurface)
                        .then(
                            if (imagePath == null) {
                                Modifier.drawBehind {
                                    val strokeWidth = 1.5.dp.toPx()
                                    drawRoundRect(
                                        color = colors.hairlineBorder,
                                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                                        cornerRadius = CornerRadius(PhotoCorner.toPx() - strokeWidth / 2f),
                                        style = Stroke(
                                            width = strokeWidth,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                                        )
                                    )
                                }
                            } else {
                                Modifier.border(0.8.dp, colors.hairlineBorder, PhotoShape)
                            }
                        )
                        .clickable { launchPhotoPicker() },
                    contentAlignment = Alignment.Center
                ) {
                    if (imagePath == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentSoftContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddPhotoAlternate,
                                    contentDescription = "Tambah foto barang",
                                    modifier = Modifier.size(24.dp),
                                    tint = colors.accentBlue
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Tambah Foto Barang",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Opsional • Ketuk untuk memilih dari galeri",
                                color = colors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        val imageRequest = remember(imagePath) {
                            ImageRequest.Builder(context)
                                .data(imagePath)
                                .size(800, 500)
                                .crossfade(150)
                                .build()
                        }
                        AsyncImage(
                            model = imageRequest,
                            contentDescription = "Foto celengan",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.elevatedSurface)
                                .clickable { viewModel.removeImage() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Hapus foto",
                                modifier = Modifier.size(16.dp),
                                tint = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // B. Tiga input utama dengan ikon di luar kolom teks.
            InputRow(iconColumnWidth, {
                Icon(
                    imageVector = Icons.Rounded.ShortText,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = colors.textPrimary
                )
            }) {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    label = { Text("Nama Tabungan") },
                    singleLine = true,
                    shape = FieldShape,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            InputRow(iconColumnWidth, {
                Icon(
                    imageVector = Icons.Outlined.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = colors.textPrimary
                )
            }) {
                OutlinedTextField(
                    value = state.targetAmountText,
                    onValueChange = viewModel::onTargetAmountChange,
                    label = { Text("Target Tabungan") },
                    singleLine = true,
                    shape = FieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            InputRow(iconColumnWidth, { CurrencyFlagIcon(state.currency) }) {
                // OutlinedTextField readOnly tetap mengonsumsi sentuhan, jadi klik
                // dipasang pada overlay matchParentSize di atasnya — bukan pada
                // modifier field (tidak pernah terpanggil) dan bukan enabled=false.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(DropdownShape)
                        .clickable { currencySheetOpen = true }
                ) {
                    OutlinedTextField(
                        value = state.currency.formLabel(),
                        onValueChange = {},
                        label = { Text("Mata Uang") },
                        readOnly = true,
                        singleLine = true,
                        shape = DropdownShape,
                        colors = fieldColors,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = colors.textSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { currencySheetOpen = true }
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(thickness = 0.8.dp, color = colors.divider)

            // C. Rencana Pengisian.
            Text(
                "Rencana Pengisian",
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            FrequencyPill(
                selected = state.planFrequency,
                onSelect = viewModel::setPlanFrequency
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.planAmountText,
                    onValueChange = viewModel::onPlanAmountChange,
                    label = { Text("Nominal Pengisian") },
                    singleLine = true,
                    shape = FieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(colors.cardSurface)
                        .border(0.8.dp, colors.hairlineBorder, CircleShape)
                        .clickable { targetDatePickerOpen = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventAvailable,
                        contentDescription = "Atur tanggal target",
                        modifier = Modifier.size(22.dp),
                        tint = colors.textPrimary
                    )
                }
            }
            val targetDateMillis = state.targetDateMillis
            Text(
                "Disisihkan setiap ${state.planFrequency.periodWord()}",
                color = colors.textMuted,
                fontSize = 12.sp
            )
            if (targetDateMillis != null) {
                Text(
                    "Target tercapai pada ${formatTargetDate(targetDateMillis)}",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Masih bingung mengisi rencana pengisian ?",
                    color = colors.textSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Hitung Dengan Kalkulator Target",
                    color = colors.accentBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(LinkShape)
                        .clickable { calculatorSheetOpen = true }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(thickness = 0.8.dp, color = colors.divider)

            // D. Notifikasi.
            Text("Notifikasi", color = colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.clickable { reminderTimePickerOpen = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.reminderHour.toString().padStart(2, '0')}:${state.reminderMinute.toString().padStart(2, '0')}",
                        color = if (state.reminderEnabled) colors.textPrimary else colors.textMuted,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Normal,
                        style = TabularStyle
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoftContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = colors.accentSoftContent
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                VerticalDivider(
                    Modifier.height(36.dp).padding(end = 18.dp),
                    thickness = 0.8.dp,
                    color = colors.divider
                )
                Switch(
                    checked = state.reminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.setReminderEnabled(enabled)
                    },
                    colors = switchColors
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 5
            ) {
                DayLabels.forEachIndexed { index, label ->
                    val bit = dayBit(index)
                    val selected = state.reminderEnabled && (state.reminderDaysMask and bit) != 0
                    val containerTarget = when {
                        selected -> colors.accentSoftContainer
                        state.reminderEnabled -> colors.cardSurface
                        else -> colors.background
                    }
                    val borderTarget = when {
                        selected -> colors.accentBlue
                        state.reminderEnabled -> colors.hairlineBorder
                        else -> colors.divider
                    }
                    val textTarget = when {
                        selected -> colors.accentSoftContent
                        state.reminderEnabled -> colors.textSecondary
                        else -> colors.textMuted
                    }
                    val containerColor by animateThemeColor(
                        targetValue = containerTarget,
                        animationSpec = MicroSpring,
                        label = "dayContainer"
                    )
                    val borderColor by animateThemeColor(
                        targetValue = borderTarget,
                        animationSpec = MicroSpring,
                        label = "dayBorder"
                    )
                    val textColor by animateThemeColor(
                        targetValue = textTarget,
                        animationSpec = MicroSpring,
                        label = "dayText"
                    )
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(ChipShape)
                            .background(containerColor)
                            .border(1.dp, borderColor, ChipShape)
                            .clickable { viewModel.toggleReminderDay(bit) }
                            .padding(horizontal = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = textColor, fontSize = 13.sp, maxLines = 1, softWrap = false)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InputRow(
    iconColumnWidth: Dp,
    icon: @Composable () -> Unit,
    field: @Composable () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(iconColumnWidth), contentAlignment = Alignment.CenterStart) { icon() }
        Box(Modifier.weight(1f)) { field() }
    }
}

@Composable
private fun CurrencyFlagIcon(currency: CurrencyCode) {
    Image(
        painter = painterResource(flagRes(currency)),
        contentDescription = currency.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(width = 26.dp, height = 18.dp).clip(FlagShape)
    )
}

@Composable
private fun FrequencyPill(selected: PlanFrequency, onSelect: (PlanFrequency) -> Unit) {
    val colors = LocalSisihinColors.current
    // Pill-track menyatu: satu kapsul luar, item tanpa border/garis pemisah.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(TrackShape)
            .background(colors.cardSurface)
            .border(0.8.dp, colors.hairlineBorder, TrackShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PlanFrequency.entries.forEach { frequency ->
            val active = frequency == selected
            val backgroundColor by animateThemeColor(
                targetValue = if (active) colors.accentSoftContainer else Color.Transparent,
                animationSpec = MicroSpring,
                label = "frequencyTrack${frequency.name}"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(TrackItemShape)
                    .background(backgroundColor)
                    .clickable { onSelect(frequency) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    frequency.label(),
                    color = if (active) colors.accentSoftContent else colors.textSecondary,
                    fontSize = 14.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
