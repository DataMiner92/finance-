package com.example.data.database

import com.example.data.model.BudgetEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.ExpenseRecord
import com.example.data.model.SavingsGoalEntity
import com.example.data.model.TransactionEntity

object PreloadData {
    val defaultCategories = listOf(
        // Expenses
        CategoryEntity(id = 1, name = "Food & Groceries", iconName = "restaurant", colorHex = "#E65100", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 2, name = "Housing & Rent", iconName = "home", colorHex = "#1565C0", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 3, name = "Utilities & Bills", iconName = "bolt", colorHex = "#F57F17", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 4, name = "Transport & Fuel", iconName = "directions_car", colorHex = "#00838F", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 5, name = "Health & Medical", iconName = "local_hospital", colorHex = "#C2185B", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 6, name = "Education & School Fees", iconName = "school", colorHex = "#6A1B9A", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 7, name = "Farm & Business Ops", iconName = "agriculture", colorHex = "#2E7D32", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 8, name = "Family Support", iconName = "volunteer_activism", colorHex = "#0277BD", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 9, name = "Debt Repayment", iconName = "credit_card", colorHex = "#AD1457", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 10, name = "Personal & Clothing", iconName = "checkroom", colorHex = "#455A64", type = "EXPENSE", isDefault = true),
        CategoryEntity(id = 11, name = "Other Expense", iconName = "more_horiz", colorHex = "#546E7A", type = "EXPENSE", isDefault = true),

        // Income
        CategoryEntity(id = 12, name = "Salary / Wages", iconName = "payments", colorHex = "#2E7D32", type = "INCOME", isDefault = true),
        CategoryEntity(id = 13, name = "Business & Sales", iconName = "store", colorHex = "#00695C", type = "INCOME", isDefault = true),
        CategoryEntity(id = 14, name = "Produce / Harvest", iconName = "yard", colorHex = "#558B2F", type = "INCOME", isDefault = true),
        CategoryEntity(id = 15, name = "Mobile Remittance", iconName = "smartphone", colorHex = "#0288D1", type = "INCOME", isDefault = true),
        CategoryEntity(id = 16, name = "Side Gig / Freelance", iconName = "work", colorHex = "#37474F", type = "INCOME", isDefault = true),
        CategoryEntity(id = 17, name = "Other Income", iconName = "savings", colorHex = "#00897B", type = "INCOME", isDefault = true)
    )

    val defaultBudgets = listOf(
        BudgetEntity(id = 1, categoryName = "Food & Groceries", monthlyLimit = 250.0, monthYear = "*"),
        BudgetEntity(id = 2, categoryName = "Utilities & Bills", monthlyLimit = 80.0, monthYear = "*"),
        BudgetEntity(id = 3, categoryName = "Transport & Fuel", monthlyLimit = 70.0, monthYear = "*"),
        BudgetEntity(id = 4, categoryName = "Housing & Rent", monthlyLimit = 400.0, monthYear = "*")
    )

    val defaultSavingsGoals = listOf(
        SavingsGoalEntity(
            id = 1,
            name = "Emergency Buffer Fund",
            targetAmount = 1000.0,
            currentAmount = 350.0,
            targetDateMillis = System.currentTimeMillis() + 180L * 24 * 3600 * 1000,
            iconName = "shield",
            colorHex = "#0D7C66",
            notes = "3 months of basic living needs safely kept in reserve."
        ),
        SavingsGoalEntity(
            id = 2,
            name = "Rainy Day & Medical",
            targetAmount = 400.0,
            currentAmount = 150.0,
            targetDateMillis = System.currentTimeMillis() + 90L * 24 * 3600 * 1000,
            iconName = "local_hospital",
            colorHex = "#C2185B",
            notes = "Health safety cushion for unexpected family clinics."
        )
    )

    // A few initial realistic starter transactions so first-time users immediately see a working financial overview
    fun initialTransactions(): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return listOf(
            TransactionEntity(
                id = 1,
                type = "INCOME",
                amount = 950.0,
                categoryName = "Salary / Wages",
                timestamp = now - (3 * day),
                note = "Monthly payment received",
                paymentMethod = "Mobile Money"
            ),
            TransactionEntity(
                id = 2,
                type = "EXPENSE",
                amount = 45.50,
                categoryName = "Food & Groceries",
                timestamp = now - (2 * day),
                note = "Market fresh produce and staples",
                paymentMethod = "Cash"
            ),
            TransactionEntity(
                id = 3,
                type = "EXPENSE",
                amount = 18.0,
                categoryName = "Transport & Fuel",
                timestamp = now - (1 * day),
                note = "Weekly commute bus fare",
                paymentMethod = "Mobile Money"
            ),
            TransactionEntity(
                id = 4,
                type = "EXPENSE",
                amount = 25.0,
                categoryName = "Utilities & Bills",
                timestamp = now - (12 * 3600_000L),
                note = "Prepaid power token top-up",
                paymentMethod = "Mobile Money"
            ),
            TransactionEntity(
                id = 5,
                type = "INCOME",
                amount = 120.0,
                categoryName = "Business & Sales",
                timestamp = now - (6 * 3600_000L),
                note = "Small trade items sale",
                paymentMethod = "Cash"
            )
        )
    }

    // Initial realistic starter expense records demonstrating the ExpenseRecord Room schema
    fun initialExpenseRecords(): List<ExpenseRecord> {
        val now = System.currentTimeMillis()
        val day = 86_400_000L
        return listOf(
            ExpenseRecord(
                id = 1,
                amount = 45.50,
                category = "Food & Groceries",
                date = now - (2 * day),
                description = "Market fresh produce and staples",
                paymentMethod = "Cash"
            ),
            ExpenseRecord(
                id = 2,
                amount = 18.0,
                category = "Transport & Fuel",
                date = now - (1 * day),
                description = "Weekly commute bus fare",
                paymentMethod = "Mobile Money"
            ),
            ExpenseRecord(
                id = 3,
                amount = 25.0,
                category = "Utilities & Bills",
                date = now - (12 * 3600_000L),
                description = "Prepaid power token top-up",
                paymentMethod = "Mobile Money"
            )
        )
    }
}
