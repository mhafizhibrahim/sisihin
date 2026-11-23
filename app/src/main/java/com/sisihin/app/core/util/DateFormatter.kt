package com.sisihin.app.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val locale = Locale("id", "ID")
    private val dateTimeFmt = SimpleDateFormat("d MMM yyyy • HH:mm", locale)
    private val dateFmt = SimpleDateFormat("d MMM yyyy", locale)
    private val timeFmt = SimpleDateFormat("HH:mm", locale)

    fun dateTime(millis: Long): String = dateTimeFmt.format(Date(millis))
    fun date(millis: Long): String = dateFmt.format(Date(millis))
    fun time(millis: Long): String = timeFmt.format(Date(millis))
    fun time(hour: Int, minute: Int): String = String.format(locale, "%02d:%02d", hour, minute)
}