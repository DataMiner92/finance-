package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import com.example.ui.components.SpeedometerMenuItem
import com.example.ui.components.SpeedometerMenuWidget
import com.example.ui.theme.SafeLedgerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SpeedometerMenuTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `speedometer menu collapses items initially and expands on click`() {
        var expenseClicked = false
        var savingsClicked = false

        composeTestRule.setContent {
            var isExpanded by remember { mutableStateOf(false) }

            val items = listOf(
                SpeedometerMenuItem(
                    id = "log_expense",
                    title = "+ Expense",
                    icon = Icons.Default.AddCircle,
                    contentDescription = "Log Expense",
                    testTag = "speedometer_item_log_expense",
                    onClick = { expenseClicked = true }
                ),
                SpeedometerMenuItem(
                    id = "savings",
                    title = "Savings",
                    icon = Icons.Default.Savings,
                    contentDescription = "Savings",
                    testTag = "speedometer_item_savings",
                    onClick = { savingsClicked = true }
                )
            )

            SafeLedgerTheme {
                SpeedometerMenuWidget(
                    isExpanded = isExpanded,
                    onToggleExpand = { isExpanded = !isExpanded },
                    items = items
                )
            }
        }

        // Trigger button is visible when collapsed
        composeTestRule.onNodeWithTag("speedometer_menu_trigger").assertExists().assertIsDisplayed()

        // Collapsed items are not visible initially
        composeTestRule.onNodeWithTag("speedometer_item_log_expense").assertDoesNotExist()
        composeTestRule.onNodeWithTag("speedometer_gauge_canvas").assertDoesNotExist()

        // Tap trigger to expand speedometer menu
        composeTestRule.onNodeWithTag("speedometer_menu_trigger").performClick()
        composeTestRule.waitForIdle()

        // Canvas and items now exist
        composeTestRule.onNodeWithTag("speedometer_gauge_canvas", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("speedometer_item_log_expense", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("speedometer_item_savings", useUnmergedTree = true).assertExists()

        // Click an item in the speedometer radial arc
        composeTestRule.onNodeWithTag("speedometer_item_log_expense", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        // Verify action executed
        assertTrue(expenseClicked)
        assertFalse(savingsClicked)
    }

    @Test
    fun `scrim tap collapses speedometer menu`() {
        composeTestRule.setContent {
            var isExpanded by remember { mutableStateOf(true) }

            val items = listOf(
                SpeedometerMenuItem(
                    id = "tips",
                    title = "Tips",
                    icon = Icons.Default.Lightbulb,
                    contentDescription = "Tips",
                    testTag = "speedometer_item_tips",
                    onClick = {}
                )
            )

            SafeLedgerTheme {
                SpeedometerMenuWidget(
                    isExpanded = isExpanded,
                    onToggleExpand = { isExpanded = !isExpanded },
                    items = items
                )
            }
        }

        // Items and scrim are expanded
        composeTestRule.onNodeWithTag("speedometer_menu_scrim", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("speedometer_item_tips", useUnmergedTree = true).assertExists()

        // Click scrim in the empty space (top area) to dismiss
        composeTestRule.onNodeWithTag("speedometer_menu_scrim", useUnmergedTree = true)
            .performTouchInput { click(androidx.compose.ui.geometry.Offset(20f, 20f)) }
        composeTestRule.waitForIdle()

        // Items should be dismissed / collapsed
        composeTestRule.onNodeWithTag("speedometer_item_tips", useUnmergedTree = true).assertDoesNotExist()
    }
}
