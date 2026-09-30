package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.ui.components.CreateNewPinDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CreateNewPinTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `creating a new PIN with matching confirmation and saving passes correct values`() {
        var savedPin = ""
        var savedHint = ""
        var dismissed = false

        composeTestRule.setContent {
            CreateNewPinDialog(
                isExistingPin = false,
                onDismiss = { dismissed = true },
                onSavePin = { pin, hint ->
                    savedPin = pin
                    savedHint = hint
                }
            )
        }

        // Check dialog title exists
        composeTestRule.onNodeWithTag("create_pin_dialog_title").assertIsDisplayed()

        // Submit button should initially be disabled because fields are empty
        composeTestRule.onNodeWithTag("store_pin_button").assertIsNotEnabled()

        // Enter new PIN
        composeTestRule.onNodeWithTag("new_pin_input").performTextInput("8421")

        // Still disabled because confirmation is missing
        composeTestRule.onNodeWithTag("store_pin_button").assertIsNotEnabled()

        // Enter non-matching confirm PIN
        composeTestRule.onNodeWithTag("confirm_pin_input").performScrollTo().performTextInput("8420")
        composeTestRule.onNodeWithText("✗ PINs do not match").assertIsDisplayed()
        composeTestRule.onNodeWithTag("store_pin_button").performScrollTo().assertIsNotEnabled()

        // Clear and enter matching confirm PIN
        composeTestRule.onNodeWithTag("confirm_pin_input").performScrollTo().performTextClearance()
        composeTestRule.onNodeWithTag("confirm_pin_input").performScrollTo().performTextInput("8421")

        // Enter security hint
        composeTestRule.onNodeWithTag("security_hint_input").performScrollTo().performTextInput("Favorite year")

        // Submit button should be enabled now
        composeTestRule.onNodeWithTag("store_pin_button").performScrollTo().assertIsEnabled()

        // Click Store PIN for Future Access
        composeTestRule.onNodeWithTag("store_pin_button").performScrollTo().performClick()

        // Assert saved values
        assertEquals("8421", savedPin)
        assertEquals("Favorite year", savedHint)
    }

    @Test
    fun `cancel button triggers onDismiss`() {
        var dismissed = false

        composeTestRule.setContent {
            CreateNewPinDialog(
                isExistingPin = true,
                onDismiss = { dismissed = true },
                onSavePin = { _, _ -> }
            )
        }

        composeTestRule.onNodeWithTag("cancel_pin_button").performScrollTo().performClick()
        assertTrue(dismissed)
    }
}
