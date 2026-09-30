package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("safeledger_prefs", Context.MODE_PRIVATE)

    private val _currencySymbol = MutableStateFlow(prefs.getString(KEY_CURRENCY_SYMBOL, "$") ?: "$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _currencyCode = MutableStateFlow(prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD")
    val currencyCode: StateFlow<String> = _currencyCode.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _isAutoBackupEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_BACKUP, true))
    val isAutoBackupEnabled: StateFlow<Boolean> = _isAutoBackupEnabled.asStateFlow()

    private val _lastBackupTimestamp = MutableStateFlow(prefs.getLong(KEY_LAST_BACKUP, 0L))
    val lastBackupTimestamp: StateFlow<Long> = _lastBackupTimestamp.asStateFlow()

    private val _autoLockTimeoutMinutes = MutableStateFlow(prefs.getInt(KEY_AUTO_LOCK_MINUTES, 5))
    val autoLockTimeoutMinutes: StateFlow<Int> = _autoLockTimeoutMinutes.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false))
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun setCurrency(symbol: String, code: String) {
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).putString(KEY_CURRENCY_CODE, code).apply()
        _currencySymbol.value = symbol
        _currencyCode.value = code
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun setAutoBackup(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_BACKUP, enabled).apply()
        _isAutoBackupEnabled.value = enabled
    }

    fun updateLastBackupTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP, timestamp).apply()
        _lastBackupTimestamp.value = timestamp
    }

    fun setAutoLockTimeoutMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_AUTO_LOCK_MINUTES, minutes).apply()
        _autoLockTimeoutMinutes.value = minutes
    }

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        _isBiometricEnabled.value = enabled
    }

    companion object {
        private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_LAST_BACKUP = "last_backup"
        private const val KEY_AUTO_LOCK_MINUTES = "auto_lock_minutes"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

        val AVAILABLE_CURRENCIES = listOf(
            CurrencyOption("$", "USD", "US Dollar ($)"),
            CurrencyOption("KSh ", "KES", "Kenyan Shilling (KSh)"),
            CurrencyOption("€", "EUR", "Euro (€)"),
            CurrencyOption("£", "GBP", "British Pound (£)"),
            CurrencyOption("₹", "INR", "Indian Rupee (₹)"),
            CurrencyOption("₦", "NGN", "Nigerian Naira (₦)"),
            CurrencyOption("R", "ZAR", "South African Rand (R)"),
            CurrencyOption("USh ", "UGX", "Ugandan Shilling (USh)"),
            CurrencyOption("TSh ", "TZS", "Tanzanian Shilling (TSh)"),
            CurrencyOption("GH₵", "GHS", "Ghanaian Cedi (GH₵)"),
            CurrencyOption("C$", "CAD", "Canadian Dollar (C$)"),
            CurrencyOption("A$", "AUD", "Australian Dollar (A$)")
        )
    }
}

data class CurrencyOption(
    val symbol: String,
    val code: String,
    val label: String
)
