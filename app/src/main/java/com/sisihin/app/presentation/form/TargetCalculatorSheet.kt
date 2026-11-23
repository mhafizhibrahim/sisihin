package com.sisihin.app.presentation.form

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.theme.animateThemeColor
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.PlanFrequency
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.ceil

private val SheetFieldShape = RoundedCornerShape(14.dp)
private val SheetCardShape = RoundedCornerShape(14.dp)
private val SheetTrackShape = RoundedCornerShape(14.dp)
private val SheetTrackItemShape = RoundedCornerShape(10.dp)
private val SheetPillShape = RoundedCornerShape(50)
private val SheetSpring = spring<Color>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)
private val SheetDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("id", "ID"))

@Composable
private fun sheetFieldColors() = with(LocalSisihinColors.current) {
    OutlinedTextFieldDefaults.colors(
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        focusedLabelColor = textSecondary,
        unfocusedLabelColor = textSecondary,
        focusedBorderColor = hairlineBorder,
        unfocusedBorderColor = hairlineBorder,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        cursorColor = accentBlue
    )
}

@Composable
private fun sheetChipColors() = with(LocalSisihinColors.current) {
    FilterChipDefaults.filterChipColors(
        containerColor = cardSurface,
        labelColor = textSecondary,
        selectedContainerColor = accentSoftContainer,
        selectedLabelColor = accentSoftContent
    )
}

private fun PlanFrequency.sheetLabel(): String = when (ordinal) {
    0 -> "Harian"
    1 -> "Mingguan"
    else -> "Bulanan"
}

/** Satuan periode: Hari / Minggu / Bulan (untuk label input & hasil). */
private fun PlanFrequency.unitLabel(): String = when (ordinal) {
    0 -> "Hari"
    1 -> "Minggu"
    else -> "Bulan"
}

private fun PlanFrequency.daysPerPeriod(): Long = when (ordinal) {
    0 -> 1L
    1 -> 7L
    else -> 30L
}

/** Chip cepat durasi (mode Cari Nominal), sesuai frekuensi. */
private fun PlanFrequency.quickDurations(): List<Long> = when (ordinal) {
    0 -> listOf(7L, 14L, 30L, 90L)
    1 -> listOf(4L, 8L, 12L, 24L)
    else -> listOf(3L, 6L, 12L, 24L)
}

private val QuickAmounts = listOf(10_000L, 20_000L, 50_000L, 100_000L)

/** Label chip nominal: angka saja tanpa simbol mata uang. */
private fun amountChipLabel(amount: Long, currency: CurrencyCode): String =
    CurrencyFormatter.format(amount, currency).removePrefix(currency.symbol).trim()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetCalculatorSheet(
    initialTargetText: String,
    initialFrequency: PlanFrequency,
    currencyCode: CurrencyCode = CurrencyCode.IDR,
    onApply: (PlanFrequency, Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSisihinColors.current
    var mode by remember { mutableStateOf(0) } // 0 = cari nominal, 1 = cari waktu
    var targetText by remember { mutableStateOf<TextFieldValue>(CurrencyFormatter.inputValue(initialTargetText)) }
    var inputText by remember { mutableStateOf<TextFieldValue>(CurrencyFormatter.inputValue("")) }
    var frequency by remember { mutableStateOf(initialFrequency) }
    // Varian kartu/input diputus sekali dari nilai awal. Bila ikut nilai live,
    // field hilang setelah digit pertama (target > 0) dan fokus ketik terputus.
    val showTargetCard = remember { CurrencyFormatter.parse(initialTargetText) > 0L }
    val fieldColors = sheetFieldColors()
    val chipColors = sheetChipColors()

    // Perhitungan live: tidak ada tombol "Hitung", hasil ikut setiap ketikan.
    val target = CurrencyFormatter.parse(targetText.text)
    val inputValue = CurrencyFormatter.parse(inputText.text)
    val periods = if (mode == 0) {
        inputValue
    } else {
        if (inputValue > 0L && target > 0L) ceil(target.toDouble() / inputValue.toDouble()).toLong() else 0L
    }
    val planAmount = if (mode == 0) {
        if (periods > 0L && target > 0L) ceil(target.toDouble() / periods.toDouble()).toLong() else 0L
    } else {
        inputValue
    }
    val valid = target > 0L && periods > 0L && planAmount > 0L
    val estimateDate = if (valid) {
        LocalDate.now().plusDays(periods * frequency.daysPerPeriod()).format(SheetDateFormatter)
    } else {
        null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = colors.elevatedSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Kalkulator Target", color = colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Hitung otomatis nominal atau lama menabung",
                    color = colors.textSecondary,
                    fontSize = 12.sp
                )
            }

            // Kartu info target; bila form utama masih kosong, sediakan input di sini.
            if (showTargetCard) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SheetCardShape)
                        .background(colors.cardSurface)
                        .border(0.8.dp, colors.hairlineBorder, SheetCardShape)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text("Target Tabungan", color = colors.textSecondary, fontSize = 12.sp)
                    Text(
                        CurrencyFormatter.format(target, currencyCode),
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = CurrencyFormatter.formatInput(it) },
                    label = { Text("Target Tabungan") },
                    supportingText = if (target <= 0L) {
                        {
                            Text(
                                "Isi Target Tabungan terlebih dahulu",
                                color = colors.textMuted,
                                fontSize = 12.sp
                            )
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    shape = SheetFieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Baris 1: mode. Baris 2: frekuensi. Pill-track bersih tanpa garis kotak.
            SheetPillTrack(
                labels = listOf("Cari Nominal", "Cari Waktu"),
                selectedIndex = mode,
                onSelect = { mode = it }
            )
            SheetPillTrack(
                labels = PlanFrequency.entries.map { it.sheetLabel() },
                selectedIndex = frequency.ordinal,
                onSelect = { frequency = PlanFrequency.entries[it] }
            )

            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = CurrencyFormatter.formatInput(it) },
                label = {
                    Text(
                        if (mode == 0) "Jumlah ${frequency.unitLabel()}"
                        else "Nominal per ${frequency.unitLabel()}"
                    )
                },
                singleLine = true,
                shape = SheetFieldShape,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 4 chip cepat adaptif, 2 per baris agar nominal panjang tidak terpotong.
            val quickValues = if (mode == 0) frequency.quickDurations() else QuickAmounts
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                quickValues.chunked(2).forEach { rowValues ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowValues.forEach { quick ->
                            FilterChip(
                                selected = inputValue == quick,
                                onClick = { inputText = CurrencyFormatter.inputValue(quick) },
                                label = {
                                    Text(
                                        text = if (mode == 0) {
                                            "$quick ${frequency.unitLabel()}"
                                        } else {
                                            amountChipLabel(quick, currencyCode)
                                        },
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                },
                                colors = chipColors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Kartu hasil live beraksen biru lembut.
            if (valid) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SheetCardShape)
                        .background(colors.accentSoftContainer)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (mode == 0) {
                            "${CurrencyFormatter.format(planAmount, currencyCode)} / ${frequency.unitLabel().lowercase()}"
                        } else {
                            "$periods ${frequency.unitLabel()}"
                        },
                        color = colors.accentSoftContent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Estimasi tercapai: $estimateDate",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = { if (valid) onApply(frequency, planAmount, target) },
                enabled = valid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .alpha(if (valid) 1f else 0.5f),
                shape = SheetPillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentBlue,
                    contentColor = colors.onAccent,
                    disabledContainerColor = colors.accentBlue,
                    disabledContentColor = colors.onAccent
                )
            ) { Text("Terapkan ke Rencana", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Pill-track menyatu: satu kapsul luar, item aktif pil penuh, tanpa border per item. */
@Composable
private fun SheetPillTrack(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    val colors = LocalSisihinColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(SheetTrackShape)
            .background(colors.cardSurface)
            .border(0.8.dp, colors.hairlineBorder, SheetTrackShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val active = index == selectedIndex
            val backgroundColor by animateThemeColor(
                targetValue = if (active) colors.accentSoftContainer else Color.Transparent,
                animationSpec = SheetSpring,
                label = "sheetTrack$label"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(SheetTrackItemShape)
                    .background(backgroundColor)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    color = if (active) colors.accentSoftContent else colors.textSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}