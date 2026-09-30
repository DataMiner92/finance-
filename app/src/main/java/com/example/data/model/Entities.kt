package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

@JsonClass(generateAdapter = true)
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // EXPENSE, INCOME, TRANSFER
    val amount: Double,
    val categoryName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val paymentMethod: String = "Cash", // Cash, Mobile Money, Bank, Card, Other
    val goalId: Long? = null
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: String,
    val type: String = "EXPENSE", // EXPENSE, INCOME, BOTH
    val isDefault: Boolean = false
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryName: String, // or "OVERALL"
    val monthlyLimit: Double,
    val monthYear: String = "*" // "*" for perpetual or "YYYY-MM"
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDateMillis: Long = 0L,
    val iconName: String = "savings",
    val colorHex: String = "#0D7C66",
    val notes: String = ""
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "backup_logs")
data class BackupLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val fileName: String,
    val filePath: String,
    val itemCount: Int,
    val sizeBytes: Long,
    val isEncrypted: Boolean = true
)

@JsonClass(generateAdapter = true)
data class FullBackupData(
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val transactions: List<TransactionEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val expenses: List<ExpenseRecord> = emptyList()
)
