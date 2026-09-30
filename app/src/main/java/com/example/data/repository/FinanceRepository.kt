package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.dao.FinanceDao
import com.example.data.model.BackupLogEntity
import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseRecord
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity
import com.example.data.security.BackupManager
import com.example.data.security.SecurityManager
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FinanceRepository(
    private val financeDao: FinanceDao,
    private val backupManager: BackupManager,
    private val securityManager: SecurityManager,
    private val expenseDao: ExpenseDao? = null
) {
    // Expense Records (Room Schema)
    val allExpenses: Flow<List<ExpenseRecord>> = expenseDao?.getAllExpenses() ?: emptyFlow()

    suspend fun insertExpense(expense: ExpenseRecord): Long =
        expenseDao?.insertExpense(expense) ?: 0L

    suspend fun deleteExpense(expense: ExpenseRecord) {
        expenseDao?.deleteExpense(expense)
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = financeDao.getAllTransactions()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> =
        financeDao.getTransactionsBetween(start, end)

    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> =
        financeDao.getRecentTransactions(limit)

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        val id = financeDao.insertTransaction(transaction)

        // Automatically store into expense_records Room schema if this is an expense
        if (transaction.type == "EXPENSE" && expenseDao != null) {
            expenseDao.insertExpense(
                ExpenseRecord(
                    amount = transaction.amount,
                    category = transaction.categoryName,
                    date = transaction.timestamp,
                    description = transaction.note,
                    paymentMethod = transaction.paymentMethod
                )
            )
        }

        // If savings goal linked, update goal amount
        if (transaction.goalId != null) {
            val goal = financeDao.getSavingsGoalById(transaction.goalId)
            if (goal != null) {
                val newAmount = if (transaction.type == "EXPENSE") {
                    (goal.currentAmount - transaction.amount).coerceAtLeast(0.0)
                } else {
                    goal.currentAmount + transaction.amount
                }
                financeDao.updateSavingsGoal(goal.copy(currentAmount = newAmount))
            }
        }
        return id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) =
        financeDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        financeDao.deleteTransaction(transaction)

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = financeDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity): Long =
        financeDao.insertCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        financeDao.deleteCategory(category)

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = financeDao.getAllBudgets()

    suspend fun insertBudget(budget: BudgetEntity): Long =
        financeDao.insertBudget(budget)

    suspend fun deleteBudget(budget: BudgetEntity) =
        financeDao.deleteBudget(budget)

    // Savings Goals
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = financeDao.getAllSavingsGoals()

    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long =
        financeDao.insertSavingsGoal(goal)

    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) =
        financeDao.updateSavingsGoal(goal)

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) =
        financeDao.deleteSavingsGoal(goal)

    // Backup & Restore
    val allBackupLogs: Flow<List<BackupLogEntity>> = financeDao.getAllBackupLogs()

    suspend fun createEncryptedBackup(): Result<BackupLogEntity> =
        backupManager.createLocalEncryptedBackup()

    suspend fun restoreFromBackup(file: File, pinOverride: String? = null): Result<Int> =
        backupManager.restoreFromBackupFile(file, pinOverride)

    fun listLocalBackupFiles(): List<File> =
        backupManager.listLocalBackupFiles()

    suspend fun exportCsv(): Result<File> =
        backupManager.exportReadableCsv()

    fun getShareIntent(file: File) =
        backupManager.getShareIntent(file)

    fun getBackupDirectoryPath(): String =
        backupManager.getBackupDirectoryPath()
}
