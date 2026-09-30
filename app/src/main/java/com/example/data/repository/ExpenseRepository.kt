package com.example.data.repository

import com.example.data.dao.ExpenseDao
import com.example.data.model.ExpenseRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository providing an abstraction layer over [ExpenseDao] operations.
 * Exposes reactive Kotlin [Flow] streams and suspend functions for database mutations.
 */
class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {
    /**
     * Reactive stream of all expense records, sorted by date in descending order.
     */
    val allExpenses: Flow<List<ExpenseRecord>> = expenseDao.getAllExpenses()

    /**
     * Reactive stream of total expenses sum, defaulting to 0.0 when no records exist.
     */
    val totalExpenseAmount: Flow<Double> = expenseDao.getTotalExpenseAmount()
        .map { it ?: 0.0 }

    /**
     * Observes expenses matching the given category.
     */
    fun getExpensesByCategory(category: String): Flow<List<ExpenseRecord>> =
        expenseDao.getExpensesByCategory(category)

    /**
     * Observes expenses occurring between [startDate] and [endDate].
     */
    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<ExpenseRecord>> =
        expenseDao.getExpensesBetweenDates(startDate, endDate)

    /**
     * Searches expenses matching the given search query string.
     */
    fun searchExpenses(query: String): Flow<List<ExpenseRecord>> =
        expenseDao.searchExpenses(query)

    /**
     * Retrieves an expense record by its ID.
     */
    suspend fun getExpenseById(id: Long): ExpenseRecord? =
        expenseDao.getExpenseByIdSync(id)

    /**
     * Inserts an expense record using individual field parameters.
     */
    suspend fun insertExpense(
        amount: Double,
        category: String,
        date: Long = System.currentTimeMillis(),
        description: String,
        paymentMethod: String = "Cash"
    ): Long {
        val expense = ExpenseRecord(
            amount = amount,
            category = category,
            date = date,
            description = description,
            paymentMethod = paymentMethod
        )
        return expenseDao.insertExpense(expense)
    }

    /**
     * Inserts an expense record entity directly.
     */
    suspend fun insertExpense(expense: ExpenseRecord): Long =
        expenseDao.insertExpense(expense)

    /**
     * Inserts multiple expense records.
     */
    suspend fun insertExpenses(expenses: List<ExpenseRecord>): List<Long> =
        expenseDao.insertExpenses(expenses)

    /**
     * Updates an existing expense record.
     */
    suspend fun updateExpense(expense: ExpenseRecord) =
        expenseDao.updateExpense(expense)

    /**
     * Deletes an existing expense record.
     */
    suspend fun deleteExpense(expense: ExpenseRecord) =
        expenseDao.deleteExpense(expense)

    /**
     * Deletes an expense record by row ID.
     */
    suspend fun deleteExpenseById(id: Long) =
        expenseDao.deleteExpenseById(id)

    /**
     * Clears all expense records.
     */
    suspend fun deleteAllExpenses() =
        expenseDao.deleteAllExpenses()

    /**
     * Returns a snapshot list of all expense records.
     */
    suspend fun getAllExpensesList(): List<ExpenseRecord> =
        expenseDao.getAllExpensesList()
}
