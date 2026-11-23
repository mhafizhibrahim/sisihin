package com.sisihin.app.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.rounded.ShortText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.domain.model.CurrencyCode
import com.sisihin.app.domain.model.TransactionType

/** Label chip cepat: angka saja tanpa simbol mata uang (mis. 10.000). */
private fun shortcutLabel(amount: Long, currency: CurrencyCode): String =
    CurrencyFormatter.format(amount, currency).removePrefix(currency.symbol).trim()

@Composable
private fun dialogFieldColors() = with(LocalSisihinColors.current) {
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
private fun dialogChipColors() = with(LocalSisihinColors.current) {
    FilterChipDefaults.filterChipColors(
        containerColor = cardSurface,
        labelColor = textSecondary,
        selectedContainerColor = accentSoftContainer,
        selectedLabelColor = accentSoftContent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordTransactionDialog(
    initialType: TransactionType = TransactionType.DEPOSIT,
    initialAmount: Long = 0L,
    initialNote: String = "",
    collected: Long,
    planAmount: Long,
    currencyCode: CurrencyCode = CurrencyCode.IDR,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Long, String) -> Unit
) {
    val colors = LocalSisihinColors.current
    var type by remember { mutableStateOf(initialType) }
    var amountText by remember {
        mutableStateOf<TextFieldValue>(
            if (initialAmount > 0) CurrencyFormatter.inputValue(initialAmount) else CurrencyFormatter.inputValue("")
        )
    }
    var note by remember { mutableStateOf(initialNote) }
    var showError by remember { mutableStateOf(false) }

    val quickAmounts = remember(planAmount) {
        val base = if (planAmount > 0) planAmount else 10_000L
        listOf(base, base * 2, base * 3, (base * 5).coerceAtLeast(100_000L)).distinct().sorted()
    }
    val amount = CurrencyFormatter.parse(amountText.text)
    val exceedsBalance = type == TransactionType.WITHDRAW && amount > collected

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.elevatedSurface,
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Catat Tabungan",
                    color = colors.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(0.8.dp, colors.hairlineBorder)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(3.dp)) {
                        listOf(TransactionType.DEPOSIT, TransactionType.WITHDRAW).forEach { option ->
                            val selected = type == option
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(if (selected) colors.accentBlue else colors.cardSurface)
                                    .clickable {
                                        type = option
                                        showError = false
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (option == TransactionType.DEPOSIT) "+ Tambah" else "- Kurangi",
                                    color = if (selected) colors.onAccent else colors.textSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.cardSurface,
                    border = BorderStroke(0.8.dp, colors.hairlineBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = {
                                amountText = CurrencyFormatter.formatInput(it)
                                showError = false
                            },
                            label = { Text("Nominal") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Payments,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = colors.textSecondary
                                )
                            },
                            colors = dialogFieldColors(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = showError,
                            supportingText = if (showError) {
                                {
                                    Text(
                                        "Nominal penarikan melebihi saldo terkumpul.",
                                        color = colors.negativeRed,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                null
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 2 chip per baris: dengan 4 chip sebaris angka seperti 100.000 terpotong.
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            quickAmounts.chunked(2).forEach { rowAmounts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowAmounts.forEach { quick ->
                                        FilterChip(
                                            selected = amount == quick,
                                            onClick = {
                                                amountText = CurrencyFormatter.inputValue(quick)
                                                showError = false
                                            },
                                            label = {
                                                Text(
                                                    text = shortcutLabel(quick, currencyCode),
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            },
                                            colors = dialogChipColors(),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("Keterangan") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.ShortText,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = colors.textSecondary
                                )
                            },
                            colors = dialogFieldColors(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = {
                    if (exceedsBalance) {
                        showError = true
                    } else if (amount > 0) {
                        onSave(type, amount, note.trim())
                    }
                },
                shape = RoundedCornerShape(999.dp),
                color = colors.accentBlue
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Simpan",
                        color = colors.onAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = colors.textSecondary) }
        }
    )
}
