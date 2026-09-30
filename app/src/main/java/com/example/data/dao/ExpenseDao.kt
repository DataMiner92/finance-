package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExpenseRecord
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for [ExpenseRecord] operations.
 *
 * Implements reactive query streams using [Flow] and asynchronous mutation methods
 * marked with [suspend] as mandated by the Room Database integration guidelines.
 */
@Dao
interface ExpenseDao {

    /**
     * Observes all expense records ordered by date descending (newest first).
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC, id DESC")
    fun getAllExpenses(): Flow<List<ExpenseRecord>>

    /**
     * Observes an expense record by its unique ID.
     */
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    fun getExpenseById(id: Long): Flow<ExpenseRecord?>

    /**
     * Fetches an individual expense record synchronously by its ID within a coroutine.
     */
    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    suspend fun getExpenseByIdSync(id: Long): ExpenseRecord?

    /**
     * Observes expense records filtered by specific category name.
     */
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: String): Flow<List<ExpenseRecord>>

    /**
     * Observes expense records that occurred within a given date range (inclusive).
     */
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<ExpenseRecord>>

    /**
     * Searches expense records matching description or category text.
     */
    @Query("SELECT * FROM expenses WHERE description LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchExpenses(query: String): Flow<List<ExpenseRecord>>

    /**
     * Observes the total cumulative sum of all recorded expenses.
     */
    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalExpenseAmount(): Flow<Double?>

    /**
     * Observes the sum of expenses within a specified date interval.
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalExpenseAmountBetween(startDate: Long, endDate: Long): Flow<Double?>

    /**
     * Retrieves all expenses as a list for export or backup routines.
     */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesList(): List<ExpenseRecord>

    /**
     * Inserts a single expense record, returning the newly generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseRecord): Long

    /**
     * Inserts multiple expense records in a single transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseRecord>): List<Long>

    /**
     * Updates an existing expense record.
     */
    @Update
    suspend fun updateExpense(expense: ExpenseRecord)

    /**
     * Deletes a specific expense record.
     */
    @Delete
    suspend fun deleteExpense(expense: ExpenseRecord)

    /**
     * Deletes an expense record by its row ID.
     */
    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    /**
     * Deletes all records from the expenses table.
     */
    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()
}
