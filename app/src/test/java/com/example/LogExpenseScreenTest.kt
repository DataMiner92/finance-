package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.BudgetProgress
import com.example.ui.screens.expenses.LogExpenseScreen
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
class LogExpenseScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleCategories = listOf(
        CategoryEntity(id = 1, name = "Food & Groceries", type = "EXPENSE", iconName = "restaurant", colorHex = "#0D7C66"),
        CategoryEntity(id = 2, name = "Transport", type = "EXPENSE", iconName = "directions_bus", colorHex = "#41B3A2"),
        CategoryEntity(id = 3, name = "Utilities", type = "EXPENSE", iconName = "bolt", colorHex = "#E76F51")
    )

    @Test
    fun `log expense screen renders text fields and category dropdown`() {
        var loggedAmount = 0.0
        var loggedCategory = ""
        var loggedDescription = ""

        composeTestRule.setContent {
            SafeLedgerTheme {
                LogExpenseScreen(
                    categories = sampleCategories,
                    budgetProgressList = emptyList(),
                    recentTransactions = emptyList(),
                    currencySymbol = "$",
                    onSaveExpense = { amount, category, description, _, _ ->
                        loggedAmount = amount
                        loggedCategory = category
                        loggedDescription = description
                    },
                    onAddNewCategory = { _, _, _, _ -> }
                )
            }
        }

        // Verify Header and Form elements exist
        composeTestRule.onNodeWithTag("log_expense_title").assertExists()
        composeTestRule.onNodeWithTag("expense_amount_input").assertExists()
        composeTestRule.onNodeWithTag("expense_category_dropdown").assertExists()
        composeTestRule.onNodeWithTag("expense_description_input").assertExists()
        composeTestRule.onNodeWithTag("log_expense_submit_button").assertExists()

        // Enter amount and description
        composeTestRule.onNodeWithTag("expense_amount_input").performScrollTo().performTextInput("45.50")
        composeTestRule.onNodeWithTag("expense_description_input").performScrollTo().performTextInput("Supermarket groceries")

        // Submit expense
        composeTestRule.onNodeWithTag("log_expense_submit_button").performScrollTo().performClick()

        // Verify callback
        assertEquals(45.50, loggedAmount, 0.001)
        assertEquals("Food & Groceries", loggedCategory)
        assertTrue(loggedDescription.contains("Supermarket groceries"))
    }

    @Test
    fun `category dropdown opens and shows options`() {
        composeTestRule.setContent {
            SafeLedgerTheme {
                LogExpenseScreen(
                    categories = sampleCategories,
                    budgetProgressList = emptyList(),
                    recentTransactions = emptyList(),
                    currencySymbol = "$",
                    onSaveExpense = { _, _, _, _, _ -> },
                    onAddNewCategory = { _, _, _, _ -> }
                )
            }
        }

        // Scroll to and click on category dropdown to expand options
        composeTestRule.onNodeWithTag("expense_category_dropdown").performScrollTo().performClick()

        // Verify category option is visible in dropdown
        composeTestRule.onNodeWithTag("category_option_Transport").assertExists()
    }
}
