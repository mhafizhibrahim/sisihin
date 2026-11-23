package com.sisihin.app.presentation.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.sisihin.app.core.theme.LocalSisihinColors
import com.sisihin.app.core.theme.spacing
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.core.util.SavingsCalculator
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.PlanFrequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorDialog(
    targetText: String,
    planFrequency: PlanFrequency,
    onApply: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = LocalSisihinColors.current
    var mode by remember { mutableStateOf(0) } // 0 nominal, 1 waktu
    var target by remember { mutableStateOf<TextFieldValue>(CurrencyFormatter.inputValue(targetText)) }
    var dateOrAmount by remember { mutableStateOf<TextFieldValue>(CurrencyFormatter.inputValue("")) }
    var result by remember { mutableStateOf("") }

    val fieldColors = with(colors) {
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

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.elevatedSurface,
        title = { Text("Kalkulator Target", color = colors.textPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    FilterChip(
                        selected = mode == 0,
                        onClick = { mode = 0 },
                        label = { Text("Cari Nominal", color = colors.textPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.cardSurface,
                            labelColor = colors.textSecondary,
                            selectedContainerColor = colors.accentSoftContainer,
                            selectedLabelColor = colors.accentSoftContent
                        )
                    )
                    FilterChip(
                        selected = mode == 1,
                        onClick = { mode = 1 },
                        label = { Text("Cari Waktu", color = colors.textPrimary) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = colors.cardSurface,
                            labelColor = colors.textSecondary,
                            selectedContainerColor = colors.accentSoftContainer,
                            selectedLabelColor = colors.accentSoftContent
                        )
                    )
                }
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = CurrencyFormatter.formatInput(it) },
                    label = { Text("Target Tabungan") },
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateOrAmount,
                    onValueChange = { dateOrAmount = CurrencyFormatter.formatInput(it) },
                    label = { Text(if (mode == 0) "Tanggal (hari)" else "Nominal Sanggup") },
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (result.isNotBlank()) Text(text = result, color = colors.textSecondary)
                Button(
                    onClick = {
                        val t = CurrencyFormatter.parse(target.text)
                        if (mode == 0) {
                            val days = dateOrAmount.text.toLongOrNull() ?: 0L
                            val cycles = if (planFrequency == PlanFrequency.DAILY) days else if (planFrequency == PlanFrequency.WEEKLY) (days / 7).coerceAtLeast(1) else (days / 30).coerceAtLeast(1)
                            val amt = if (cycles > 0) t / cycles else 0L
                            result = "Nominal per periode: ${CurrencyFormatter.format(amt, CurrencyCode.IDR)}"
                            onApply(amt)
                        } else {
                            val amt = CurrencyFormatter.parse(dateOrAmount.text)
                            val cycles = SavingsCalculator.cyclesForTarget(t, amt)
                            result = "Estimasi ${cycles} periode"
                            onApply(amt)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentBlue,
                        contentColor = colors.onAccent
                    )
                ) { Text("Hitung", color = colors.onAccent) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tutup", color = colors.accentSoftContent) }
        },
        dismissButton = {}
    )
}
