package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.MonthlyExpenseChartCard
import com.example.ui.theme.SafeLedgerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MonthlyExpenseChartCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCategories = listOf(
        CategoryEntity(id = 1, name = "Food & Dining", iconName = "restaurant", colorHex = "#FF5722"),
        CategoryEntity(id = 2, name = "Rent & Housing", iconName = "home", colorHex = "#3F51B5"),
        CategoryEntity(id = 3, name = "Transportation", iconName = "directions_car", colorHex = "#009688")
    )

    private val sampleTransactions = listOf(
        TransactionEntity(
            id = 1,
            type = "EXPENSE",
            amount = 350.0,
            categoryName = "Food & Dining",
            timestamp = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = 2,
            type = "EXPENSE",
            amount = 750.0,
            categoryName = "Rent & Housing",
            timestamp = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = 3,
            type = "EXPENSE",
            amount = 120.0,
            categoryName = "Transportation",
            timestamp = System.currentTimeMillis()
        ),
        TransactionEntity(
            id = 4,
            type = "INCOME",
            amount = 2500.0,
            categoryName = "Salary",
            timestamp = System.currentTimeMillis()
        )
    )

    @Test
    fun `monthly expense chart renders donut canvas, center metric and category chips`() {
        var clickedCategory: String? = null

        composeTestRule.setContent {
            SafeLedgerTheme {
                MonthlyExpenseChartCard(
                    transactions = sampleTransactions,
                    categories = sampleCategories,
                    currencySymbol = "$",
                    totalExpense = 1220.0,
                    onCategoryClick = { clickedCategory = it }
                )
            }
        }

        // Summary Card exists
        composeTestRule.onNodeWithTag("monthly_expenses_chart_card").assertExists().assertIsDisplayed()

        // Canvas for Recharts / D3 Donut exists
        composeTestRule.onNodeWithTag("d3_recharts_donut_canvas", useUnmergedTree = true).assertExists()

        // Center metric exists and shows total spent
        composeTestRule.onNodeWithTag("donut_center_metric", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("TOTAL SPENT", substring = true, useUnmergedTree = true).assertExists()

        // Category breakdown chips exist
        composeTestRule.onNodeWithTag("category_chip_food_&_dining", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("category_chip_rent_&_housing", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("category_chip_transportation", useUnmergedTree = true).assertExists()

        // Tapping a category chip selects it and triggers callback
        composeTestRule.onNodeWithTag("category_chip_food_&_dining", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        assertEquals("Food & Dining", clickedCategory)
    }

    @Test
    fun `monthly expense chart displays empty state when no expenses logged`() {
        var logExpenseClicked = false

        composeTestRule.setContent {
            SafeLedgerTheme {
                MonthlyExpenseChartCard(
                    transactions = emptyList(),
                    categories = sampleCategories,
                    currencySymbol = "$",
                    totalExpense = 0.0,
                    onLogExpenseClick = { logExpenseClicked = true }
                )
            }
        }

        // Shows empty state notice
        composeTestRule.onNodeWithText("No expenses this month").assertExists().assertIsDisplayed()
        composeTestRule.onNodeWithText("Log Expense").assertExists().performClick()

        assertTrue(logExpenseClicked)
    }
}
