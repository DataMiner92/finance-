package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.dao.ExpenseDao
import com.example.data.database.AppDatabase
import com.example.data.model.ExpenseRecord
import com.example.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExpenseRoomDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var expenseDao: ExpenseDao
    private lateinit var expenseRepository: ExpenseRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        expenseDao = database.expenseDao()
        expenseRepository = ExpenseRepository(expenseDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun `insert expense record with required amount, category, date and description`() = runBlocking {
        val testDate = 1774396800000L // Specific epoch millis date
        val expense = ExpenseRecord(
            amount = 54.75,
            category = "Groceries",
            date = testDate,
            description = "Organic vegetables and fruits",
            paymentMethod = "Cash"
        )

        val id = expenseDao.insertExpense(expense)
        assertTrue("Generated ID should be positive", id > 0)

        val retrieved = expenseDao.getExpenseByIdSync(id)
        assertNotNull(retrieved)
        assertEquals(54.75, retrieved!!.amount, 0.001)
        assertEquals("Groceries", retrieved.category)
        assertEquals(testDate, retrieved.date)
        assertEquals("Organic vegetables and fruits", retrieved.description)
        assertEquals("Cash", retrieved.paymentMethod)
    }

    @Test
    fun `observe all expenses via Flow ordered by date descending`() = runBlocking {
        val baseDate = System.currentTimeMillis()
        val expense1 = ExpenseRecord(
            amount = 10.0,
            category = "Transport",
            date = baseDate - 2000,
            description = "Bus ticket"
        )
        val expense2 = ExpenseRecord(
            amount = 85.0,
            category = "Utilities",
            date = baseDate, // More recent
            description = "Electric bill"
        )

        expenseDao.insertExpense(expense1)
        expenseDao.insertExpense(expense2)

        val allExpenses = expenseDao.getAllExpenses().first()
        assertEquals(2, allExpenses.size)
        // Order is date DESC, so expense2 comes first
        assertEquals("Electric bill", allExpenses[0].description)
        assertEquals("Bus ticket", allExpenses[1].description)
    }

    @Test
    fun `filter expenses by category`() = runBlocking {
        expenseDao.insertExpense(
            ExpenseRecord(amount = 25.0, category = "Dining", date = 1000L, description = "Lunch")
        )
        expenseDao.insertExpense(
            ExpenseRecord(amount = 40.0, category = "Transport", date = 2000L, description = "Taxi")
        )
        expenseDao.insertExpense(
            ExpenseRecord(amount = 15.0, category = "Dining", date = 3000L, description = "Coffee")
        )

        val diningExpenses = expenseDao.getExpensesByCategory("Dining").first()
        assertEquals(2, diningExpenses.size)
        assertTrue(diningExpenses.all { it.category == "Dining" })
    }

    @Test
    fun `filter expenses between date range`() = runBlocking {
        expenseDao.insertExpense(ExpenseRecord(amount = 10.0, category = "Food", date = 100L, description = "D1"))
        expenseDao.insertExpense(ExpenseRecord(amount = 20.0, category = "Food", date = 200L, description = "D2"))
        expenseDao.insertExpense(ExpenseRecord(amount = 30.0, category = "Food", date = 300L, description = "D3"))
        expenseDao.insertExpense(ExpenseRecord(amount = 40.0, category = "Food", date = 400L, description = "D4"))

        val rangeExpenses = expenseDao.getExpensesBetweenDates(150L, 350L).first()
        assertEquals(2, rangeExpenses.size)
        assertEquals("D3", rangeExpenses[0].description)
        assertEquals("D2", rangeExpenses[1].description)
    }

    @Test
    fun `aggregate total expense amount calculation`() = runBlocking {
        expenseDao.insertExpense(ExpenseRecord(amount = 15.50, category = "Health", date = 1000L, description = "Meds"))
        expenseDao.insertExpense(ExpenseRecord(amount = 34.50, category = "Leisure", date = 2000L, description = "Books"))

        val total = expenseDao.getTotalExpenseAmount().first()
        assertNotNull(total)
        assertEquals(50.0, total!!, 0.001)
    }

    @Test
    fun `update and delete expense record`() = runBlocking {
        val id = expenseDao.insertExpense(
            ExpenseRecord(amount = 100.0, category = "Hardware", date = 5000L, description = "Tools")
        )

        val original = expenseDao.getExpenseByIdSync(id)!!
        val updated = original.copy(amount = 120.0, description = "Upgraded tools")
        expenseDao.updateExpense(updated)

        val retrievedAfterUpdate = expenseDao.getExpenseByIdSync(id)
        assertEquals(120.0, retrievedAfterUpdate?.amount ?: 0.0, 0.001)
        assertEquals("Upgraded tools", retrievedAfterUpdate?.description)

        expenseDao.deleteExpenseById(id)
        val retrievedAfterDelete = expenseDao.getExpenseByIdSync(id)
        assertNull(retrievedAfterDelete)
    }

    @Test
    fun `repository pattern manages expense operations successfully`() = runBlocking {
        val id = expenseRepository.insertExpense(
            amount = 75.0,
            category = "Education",
            date = 8000L,
            description = "Online course subscription",
            paymentMethod = "Card"
        )
        assertTrue(id > 0)

        val total = expenseRepository.totalExpenseAmount.first()
        assertEquals(75.0, total, 0.001)

        val searchResults = expenseRepository.searchExpenses("subscription").first()
        assertEquals(1, searchResults.size)
        assertEquals("Education", searchResults[0].category)
    }
}
