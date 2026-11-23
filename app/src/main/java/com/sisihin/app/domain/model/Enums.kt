package com.sisihin.app.domain.model

enum class PlanFrequency { DAILY, WEEKLY, MONTHLY }

enum class GoalStatus { ONGOING, ACHIEVED }

enum class TransactionType { DEPOSIT, WITHDRAW }

enum class CurrencyCode(val symbol: String, val flag: String, val label: String) {
    IDR("Rp", "🇮🇩", "Indonesia Rupiah (Rp)"),
    USD("$", "🇺🇸", "US Dollar ($)"),
    JPY("¥", "🇯🇵", "Japanese Yen (¥)"),
    EUR("€", "🇪🇺", "Euro (€)"),
    SGD("S$", "🇸🇬", "Singapore Dollar (S$)"),
    MYR("RM", "🇲🇾", "Malaysian Ringgit (RM)"),
    GBP("£", "🇬🇧", "British Pound (£)")
}

enum class SortOption(val label: String) {
    TARGET("Target Tabungan"),
    PERCENT("Persentase"),
    CREATED("Tanggal Dibuat"),
    ESTIMATE("Estimasi"),
    NAME("Nama")
}

enum class SortDirection(val label: String) {
    ASC("Meningkat"),
    DESC("Menurun")
}

enum class AppThemeMode(val label: String) {
    SYSTEM("Sistem"),
    DARK("Gelap"),
    LIGHT("Terang")
}