package com.sisihin.app.core.util

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sisihin.app.domain.model.CurrencyCode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    private val localeId = Locale("id", "ID")
    private val groupingSymbols = DecimalFormatSymbols(localeId).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    // NumberFormat/DecimalFormat tidak thread-safe: satu instance per thread, dibuat sekali.
    // Menghindari alokasi formatter di dalam composable saat scroll.
    private val amountFormatters = object : ThreadLocal<MutableMap<String, DecimalFormat>>() {
        override fun initialValue(): MutableMap<String, DecimalFormat> = mutableMapOf()
    }
    private val inputFormatter = object : ThreadLocal<DecimalFormat>() {
        override fun initialValue(): DecimalFormat = DecimalFormat("#,##0", groupingSymbols)
    }

    fun format(amount: Long, currency: CurrencyCode): String {
        val pattern = if (currency == CurrencyCode.IDR || currency == CurrencyCode.JPY) "#,##0" else "#,##0.00"
        val formatter = amountFormatters.get()!!.getOrPut(pattern) { DecimalFormat(pattern, groupingSymbols) }
        return "${currency.symbol} ${formatter.format(amount.toDouble())}".trim()
    }

    fun parse(input: String): Long {
        val digits = input.filter { it.isDigit() }
        return digits.toLongOrNull() ?: 0L
    }

    fun formatInput(input: String): String {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        val value = digits.toLongOrNull() ?: return ""
        return inputFormatter.get()!!.format(value)
    }

    /**
     * Versi untuk field input: memformat teks sambil memetakan cursor ke batas digit
     * yang sama. Tanpa ini, penyisipan titik ribuan membuat cursor tertinggal satu
     * karakter (mis. `5000` tampil `5.00|0`).
     */
    fun formatInput(value: TextFieldValue): TextFieldValue {
        val formatted = formatInput(value.text)
        if (formatted.isEmpty()) return TextFieldValue("")
        val cursor = value.selection.start.coerceIn(0, value.text.length)
        val digitsBeforeCursor = value.text.substring(0, cursor).count { it.isDigit() }
        var digitsSeen = 0
        var index = 0
        while (index < formatted.length && digitsSeen < digitsBeforeCursor) {
            if (formatted[index].isDigit()) digitsSeen++
            index++
        }
        return TextFieldValue(formatted, TextRange(index))
    }

    /** Nilai awal atau isian chip: teks terformat dengan cursor di ujung. */
    fun inputValue(text: String): TextFieldValue {
        val formatted = formatInput(text)
        return TextFieldValue(formatted, TextRange(formatted.length))
    }

    fun inputValue(amount: Long): TextFieldValue = inputValue(amount.toString())
}
