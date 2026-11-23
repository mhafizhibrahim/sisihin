package com.sisihin.app.presentation.form

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import kotlin.math.ceil

private val SheetShape = RoundedCornerShape(8.dp)
private val SheetPillShape = RoundedCornerShape(50)
private val SheetSpring = spring<Color>(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessMediumLow
)

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

private fun PlanFrequency.sheetLabel(): String = when (ordinal) {
    0 -> "Harian"
    1 -> "Mingguan"
    else -> "Bulanan"
}

private fun PlanFrequency.daysPerPeriod(): Long = when (ordinal) {
    0 -> 1L
    1 -> 7L
    else -> 30L
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetCalculatorSheet(
    initialTargetText: String,
    initialFrequency: PlanFrequency,
    onApply: (PlanFrequency, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSisihinColors.current
    var mode by remember { mutableStateOf(0) } // 0 = cari nominal, 1 = cari estimasi waktu
    var targetText by remember { mutableStateOf<TextFieldValue>(CurrencyFormatter.inputValue(initialTargetText)) }
    var inputText by remember { mutableStateOf<TextFieldValue>(CurrencyFormatter.inputValue("")) }
    var frequency by remember { mutableStateOf(initialFrequency) }
    var resultText by remember { mutableStateOf("") }
    var computedAmount by remember { mutableStateOf(0L) }
    val fieldColors = sheetFieldColors()

    fun recompute() {
        val target = CurrencyFormatter.parse(targetText.text)
        if (mode == 0) {
            val days = inputText.text.filter { it.isDigit() }.toLongOrNull() ?: 0L
            val periods = if (days <= 0L) 0L else ceil(days.toDouble() / frequency.daysPerPeriod().toDouble()).toLong().coerceAtLeast(1L)
            val amount = if (periods > 0L) ceil(target.toDouble() / periods.toDouble()).toLong() else 0L
            computedAmount = amount
            resultText = if (amount > 0L) {
                "Nominal per ${frequency.sheetLabel().lowercase()}: ${CurrencyFormatter.format(amount, CurrencyCode.IDR)}"
            } else {
                "Isi target dan jumlah hari terlebih dahulu"
            }
        } else {
            val amount = CurrencyFormatter.parse(inputText.text)
            computedAmount = amount
            val periods = if (amount > 0L && target > 0L) ceil(target.toDouble() / amount.toDouble()).toLong() else 0L
            resultText = if (periods > 0L) {
                "Estimasi $periods ${frequency.sheetLabel().lowercase()} (${
                    periods * frequency.daysPerPeriod()
                } hari)"
            } else {
                "Isi target dan nominal per periode terlebih dahulu"
            }
        }
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
            Text("Kalkulator Target", color = colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetModeChip("Cari Nominal", mode == 0, Modifier.weight(1f)) { mode = 0; resultText = ""; computedAmount = 0L }
                SheetModeChip("Cari Waktu", mode == 1, Modifier.weight(1f)) { mode = 1; resultText = ""; computedAmount = 0L }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanFrequency.entries.forEach { option ->
                    SheetFrequencyChip(option, frequency == option, Modifier.weight(1f)) {
                        frequency = option
                        resultText = ""
                        computedAmount = 0L
                    }
                }
            }

            OutlinedTextField(
                value = targetText,
                onValueChange = {
                    targetText = CurrencyFormatter.formatInput(it)
                    resultText = ""
                    computedAmount = 0L
                },
                label = { Text("Target Tabungan") },
                singleLine = true,
                shape = SheetShape,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = inputText,
                onValueChange = {
                    inputText = CurrencyFormatter.formatInput(it)
                    resultText = ""
                    computedAmount = 0L
                },
                label = { Text(if (mode == 0) "Jumlah Hari" else "Nominal Per Periode") },
                singleLine = true,
                shape = SheetShape,
                colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { recompute() },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = SheetShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.cardSurface,
                    contentColor = colors.textPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) { Text("Hitung", fontSize = 14.sp, fontWeight = FontWeight.Medium) }

            if (resultText.isNotBlank()) {
                Text(resultText, color = colors.textSecondary, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    if (resultText.isBlank()) recompute()
                    if (computedAmount > 0L) onApply(frequency, computedAmount)
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = SheetPillShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentBlue,
                    contentColor = colors.onAccent
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) { Text("Terapkan", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SheetModeChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val colors = LocalSisihinColors.current
    val background by animateThemeColor(
        targetValue = if (selected) colors.accentSoftContainer else colors.cardSurface,
        animationSpec = SheetSpring,
        label = "calculatorMode"
    )
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(SheetShape)
            .background(background)
            .border(0.8.dp, colors.hairlineBorder, SheetShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = if (selected) colors.accentSoftContent else colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SheetFrequencyChip(
    frequency: PlanFrequency,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colors = LocalSisihinColors.current
    val background by animateThemeColor(
        targetValue = if (selected) colors.accentSoftContainer else colors.cardSurface,
        animationSpec = SheetSpring,
        label = "calculatorFrequency"
    )
    Box(
        modifier = modifier
            .height(38.dp)
            .clip(SheetShape)
            .background(background)
            .border(0.8.dp, colors.hairlineBorder, SheetShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            frequency.sheetLabel(),
            color = if (selected) colors.accentSoftContent else colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
