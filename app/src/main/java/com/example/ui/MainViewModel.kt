package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.BackupLogEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseRecord
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.preferences.AppPreferences
import com.example.data.repository.ExpenseRepository
import com.example.data.repository.FinanceRepository
import com.example.data.security.BackupManager
import com.example.data.security.SecurityManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MonthSummary(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netCashflow: Double = 0.0,
    val savingsRatePercentage: Double = 0.0,
    val transactionsCount: Int = 0
)

data class BudgetProgress(
    val budget: BudgetEntity,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progressFraction: Float, // 0.0 to 1.0+
    val isOverBudget: Boolean,
    val dailyAllowance: Double
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val preferences = AppPreferences(application)
    val securityManager = SecurityManager(application)
    private val backupManager = BackupManager(
        application,
        db.financeDao(),
        securityManager,
        preferences
    )
    val expenseRepository = ExpenseRepository(db.expenseDao())
    val repository = FinanceRepository(
        db.financeDao(),
        backupManager,
        securityManager,
        db.expenseDao()
    )

    // App Preferences State
    val currencySymbol = preferences.currencySymbol
    val currencyCode = preferences.currencyCode
    val themeMode = preferences.themeMode
    val isAutoBackupEnabled = preferences.isAutoBackupEnabled
    val lastBackupTimestamp = preferences.lastBackupTimestamp
    val autoLockTimeoutMinutes = preferences.autoLockTimeoutMinutes

    // Security State
    val isLocked: StateFlow<Boolean> = securityManager.isLocked
    val isPinConfigured: StateFlow<Boolean> = securityManager.isPinConfigured
    val isBiometricEnabled: StateFlow<Boolean> = preferences.isBiometricEnabled

    // Filter Month (e.g. "2026-09")
    private val _selectedMonth = MutableStateFlow(getCurrentYearMonth())
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    // All Reactive Data Flows
    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBackupLogs: StateFlow<List<BackupLogEntity>> = repository.allBackupLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dedicated Expense Records (Room Schema: amount, category, date, description)
    val allExpenses: StateFlow<List<ExpenseRecord>> = expenseRepository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalExpenseAmount: StateFlow<Double> = expenseRepository.totalExpenseAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Month Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        selectedMonth
    ) { transactions, monthStr ->
        val (startMillis, endMillis) = getMonthRangeMillis(monthStr)
        transactions.filter { it.timestamp in startMillis..endMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Month Summary
    val monthSummary: StateFlow<MonthSummary> = filteredTransactions.combine(allTransactions) { list, _ ->
        var income = 0.0
        var expense = 0.0
        for (t in list) {
            when (t.type) {
                "INCOME" -> income += t.amount
                "EXPENSE" -> expense += t.amount
            }
        }
        val net = income - expense
        val rate = if (income > 0) ((income - expense) / income * 100).coerceAtLeast(0.0) else 0.0
        MonthSummary(
            totalIncome = income,
            totalExpense = expense,
            netCashflow = net,
            savingsRatePercentage = rate,
            transactionsCount = list.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthSummary())

    // Budget Progress calculations
    val budgetProgressList: StateFlow<List<BudgetProgress>> = combine(
        allBudgets,
        filteredTransactions
    ) { budgets, transactions ->
        val remainingDays = getRemainingDaysInCurrentMonth()
        budgets.map { budget ->
            val spent = if (budget.categoryName.equals("OVERALL", ignoreCase = true)) {
                transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            } else {
                transactions.filter { it.type == "EXPENSE" && it.categoryName == budget.categoryName }
                    .sumOf { it.amount }
            }
            val remaining = (budget.monthlyLimit - spent).coerceAtLeast(0.0)
            val fraction = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
            val isOver = spent > budget.monthlyLimit
            val dailyAllowance = if (remainingDays > 0) remaining / remainingDays else 0.0

            BudgetProgress(
                budget = budget,
                spentAmount = spent,
                remainingAmount = budget.monthlyLimit - spent,
                progressFraction = fraction,
                isOverBudget = isOver,
                dailyAllowance = dailyAllowance
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Action Status Messages
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // --- Actions ---

    fun addTransaction(
        type: String,
        amount: Double,
        categoryName: String,
        timestamp: Long,
        note: String,
        paymentMethod: String,
        goalId: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    amount = amount,
                    categoryName = categoryName,
                    timestamp = timestamp,
                    note = note,
                    paymentMethod = paymentMethod,
                    goalId = goalId
                )
            )
            triggerAutoBackupIfEnabled()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            triggerAutoBackupIfEnabled()
        }
    }

    // Expense Record Operations (Room Schema)
    fun addExpenseRecord(
        amount: Double,
        category: String,
        date: Long = System.currentTimeMillis(),
        description: String,
        paymentMethod: String = "Cash"
    ) {
        viewModelScope.launch {
            expenseRepository.insertExpense(
                amount = amount,
                category = category,
                date = date,
                description = description,
                paymentMethod = paymentMethod
            )
            triggerAutoBackupIfEnabled()
        }
    }

    fun deleteExpenseRecord(expense: ExpenseRecord) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
            triggerAutoBackupIfEnabled()
        }
    }

    fun addCategory(name: String, iconName: String, colorHex: String, type: String) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    iconName = iconName,
                    colorHex = colorHex,
                    type = type
                )
            )
            triggerAutoBackupIfEnabled()
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            triggerAutoBackupIfEnabled()
        }
    }

    fun saveBudget(categoryName: String, limit: Double) {
        viewModelScope.launch {
            val existing = allBudgets.value.find { it.categoryName == categoryName }
            if (existing != null) {
                repository.insertBudget(existing.copy(monthlyLimit = limit))
            } else {
                repository.insertBudget(
                    BudgetEntity(
                        categoryName = categoryName,
                        monthlyLimit = limit,
                        monthYear = "*"
                    )
                )
            }
            triggerAutoBackupIfEnabled()
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
            triggerAutoBackupIfEnabled()
        }
    }

    fun addSavingsGoal(
        name: String,
        targetAmount: Double,
        currentAmount: Double,
        targetDateMillis: Long,
        iconName: String,
        colorHex: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoalEntity(
                    name = name.trim(),
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDateMillis = targetDateMillis,
                    iconName = iconName,
                    colorHex = colorHex,
                    notes = notes
                )
            )
            triggerAutoBackupIfEnabled()
        }
    }

    fun updateGoalAmount(goal: SavingsGoalEntity, delta: Double, isDeposit: Boolean) {
        viewModelScope.launch {
            val newAmount = if (isDeposit) {
                goal.currentAmount + delta
            } else {
                (goal.currentAmount - delta).coerceAtLeast(0.0)
            }
            repository.updateSavingsGoal(goal.copy(currentAmount = newAmount))
            // Also register a transaction record so cash flow accurately accounts for savings transfer
            repository.insertTransaction(
                TransactionEntity(
                    type = if (isDeposit) "EXPENSE" else "INCOME",
                    amount = delta,
                    categoryName = "Savings Transfer",
                    timestamp = System.currentTimeMillis(),
                    note = if (isDeposit) "Deposit to ${goal.name}" else "Withdrawal from ${goal.name}",
                    paymentMethod = "Internal Transfer",
                    goalId = goal.id
                )
            )
            triggerAutoBackupIfEnabled()
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
            triggerAutoBackupIfEnabled()
        }
    }

    fun setSelectedMonth(monthStr: String) {
        _selectedMonth.value = monthStr
    }

    // --- Security & Lock ---

    fun unlockApp(pin: String): Boolean {
        val ok = securityManager.verifyPin(pin)
        if (!ok) {
            _statusMessage.value = "Incorrect PIN. Please try again."
        }
        return ok
    }

    fun setupPin(pin: String, hint: String): Boolean {
        val result = securityManager.setPin(pin, hint)
        if (result) {
            _statusMessage.value = "App lock PIN enabled with AES encryption."
        }
        return result
    }

    fun removePin(): Boolean {
        val result = securityManager.removePin()
        if (result) {
            _statusMessage.value = "App lock PIN disabled."
        }
        return result
    }

    fun getSecurityHint(): String = securityManager.getSecurityHint()

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.setBiometricEnabled(enabled)
        _statusMessage.value = if (enabled) "Biometric authentication (fingerprint/face) enabled." else "Biometric authentication disabled."
    }

    fun unlockWithBiometrics() {
        securityManager.unlockDirectly()
    }

    fun lockNow() {
        securityManager.lockNow()
    }

    fun onUserInteraction() {
        securityManager.recordActivity()
    }

    fun checkAutoLock() {
        securityManager.checkAndApplyAutoLock(autoLockTimeoutMinutes.value)
    }

    // --- Preferences & Settings ---

    fun setCurrency(symbol: String, code: String) {
        preferences.setCurrency(symbol, code)
        _statusMessage.value = "Currency updated to $code ($symbol)"
    }

    fun setThemeMode(mode: String) {
        preferences.setThemeMode(mode)
    }

    fun setAutoBackup(enabled: Boolean) {
        preferences.setAutoBackup(enabled)
        _statusMessage.value = if (enabled) "Automated local backups enabled" else "Automated backups disabled"
    }

    fun setAutoLockTimeout(minutes: Int) {
        preferences.setAutoLockTimeoutMinutes(minutes)
        _statusMessage.value = if (minutes == 0) "Auto-lock set to Immediate" else "Auto-lock set to $minutes minutes"
    }

    // --- Backups & Restore ---

    fun performManualBackup() {
        viewModelScope.launch {
            val result = repository.createEncryptedBackup()
            result.onSuccess { log ->
                _statusMessage.value = "Encrypted backup saved: ${log.fileName} (${log.itemCount} records)"
            }.onFailure { err ->
                _statusMessage.value = "Backup failed: ${err.message}"
            }
        }
    }

    fun restoreBackup(file: File, pinOverride: String? = null) {
        viewModelScope.launch {
            val result = repository.restoreFromBackup(file, pinOverride)
            result.onSuccess { count ->
                _statusMessage.value = "Restored $count records successfully from ${file.name}!"
            }.onFailure { err ->
                _statusMessage.value = "Restore error: ${err.message}. Ensure valid password/file."
            }
        }
    }

    fun listBackupFiles(): List<File> = repository.listLocalBackupFiles()

    fun exportCsv(onReady: (File) -> Unit) {
        viewModelScope.launch {
            val result = repository.exportCsv()
            result.onSuccess { file ->
                _statusMessage.value = "Exported CSV to ${file.name}"
                onReady(file)
            }.onFailure { err ->
                _statusMessage.value = "Export failed: ${err.message}"
            }
        }
    }

    fun getShareIntent(file: File): Intent = repository.getShareIntent(file)

    fun getBackupFolderDirectory(): String = repository.getBackupDirectoryPath()

    private fun triggerAutoBackupIfEnabled() {
        if (isAutoBackupEnabled.value) {
            viewModelScope.launch {
                repository.createEncryptedBackup()
            }
        }
    }

    companion object {
        fun getCurrentYearMonth(): String {
            val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
            return sdf.format(Date())
        }

        fun getMonthRangeMillis(yearMonth: String): Pair<Long, Long> {
            val parts = yearMonth.split("-")
            val cal = Calendar.getInstance()
            if (parts.size == 2) {
                val year = parts[0].toIntOrNull() ?: cal.get(Calendar.YEAR)
                val month = (parts[1].toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
                cal.set(year, month, 1, 0, 0, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                cal.set(Calendar.DAY_OF_MONTH, maxDay)
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                cal.set(Calendar.MILLISECOND, 999)
                val end = cal.timeInMillis
                return Pair(start, end)
            }
            return Pair(0L, Long.MAX_VALUE)
        }

        fun getRemainingDaysInCurrentMonth(): Int {
            val cal = Calendar.getInstance()
            val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)
            return (maxDay - currentDay + 1).coerceAtLeast(1)
        }
    }
}
