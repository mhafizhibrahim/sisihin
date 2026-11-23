package com.sisihin.app

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.sisihin.app.core.util.CurrencyFormatter
import com.sisihin.app.domain.model.CurrencyCode
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyFormatterTest {
    @Test
    fun parseStripsNonDigits() {
        assertEquals(15000L, CurrencyFormatter.parse("15.000"))
    }
    @Test
    fun formatIdr() {
        assertEquals("Rp 15.000", CurrencyFormatter.format(15000, CurrencyCode.IDR))
    }
    @Test
    fun formatInputKeepsCursorAtEndWhenGroupingAppears() {
        // Mengetik digit ke-4: "500" + "0" -> "5.000", cursor harus ikut ke ujung.
        val result = CurrencyFormatter.formatInput(TextFieldValue("5000", TextRange(4)))
        assertEquals("5.000", result.text)
        assertEquals(5, result.selection.start)
    }
    @Test
    fun formatInputMapsCursorWhenEditingInTheMiddle() {
        // Menyisipkan "9" setelah dua digit pertama dari "5.000".
        val result = CurrencyFormatter.formatInput(TextFieldValue("5.0900", TextRange(4)))
        assertEquals("50.900", result.text)
        assertEquals(4, result.selection.start)
    }
    @Test
    fun formatInputHandlesDeletionAndEmptyInput() {
        val deleted = CurrencyFormatter.formatInput(TextFieldValue("1.000", TextRange(2)))
        assertEquals("1.000", deleted.text)
        assertEquals(1, deleted.selection.start)

        val empty = CurrencyFormatter.formatInput(TextFieldValue("", TextRange.Zero))
        assertEquals("", empty.text)
        assertEquals(0, empty.selection.start)
    }
    @Test
    fun inputValuePlacesCursorAtEnd() {
        val result = CurrencyFormatter.inputValue(5000L)
        assertEquals("5.000", result.text)
        assertEquals(5, result.selection.start)
    }
}
