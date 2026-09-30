package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.preferences.AppPreferences
import com.example.data.security.BiometricAuthManager
import com.example.data.security.BiometricStatus
import com.example.ui.screens.security.SecurityBackupScreen
import com.example.ui.theme.SafeLedgerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BiometricSecurityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `preferences stores and toggles biometric setting`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val prefs = AppPreferences(context)

        assertFalse(prefs.isBiometricEnabled.value)

        prefs.setBiometricEnabled(true)
        assertTrue(prefs.isBiometricEnabled.value)

        prefs.setBiometricEnabled(false)
        assertFalse(prefs.isBiometricEnabled.value)
    }

    @Test
    fun `biometric manager checks status safely`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val status = BiometricAuthManager.getBiometricStatus(context)
        assertNotNull(status)
        assertTrue(
            status in listOf(
                BiometricStatus.AVAILABLE,
                BiometricStatus.NOT_ENROLLED,
                BiometricStatus.NO_HARDWARE,
                BiometricStatus.HW_UNAVAILABLE
            )
        )
    }

    @Test
    fun `security screen renders biometric security card and toggle`() {
        var toggledBiometric: Boolean? = null

        composeTestRule.setContent {
            SafeLedgerTheme {
                SecurityBackupScreen(
                    isPinConfigured = true,
                    isAutoBackupEnabled = true,
                    lastBackupTimestamp = 0L,
                    autoLockTimeoutMinutes = 5,
                    currencyCode = "USD",
                    currencySymbol = "$",
                    themeMode = "SYSTEM",
                    backupFolderPath = "/mock/backup",
                    onSetupPin = { _, _ -> true },
                    onRemovePin = { true },
                    onLockNow = {},
                    onSetAutoBackup = {},
                    onSetAutoLockTimeout = {},
                    onSetCurrency = { _, _ -> },
                    onSetThemeMode = {},
                    onPerformManualBackup = {},
                    onRestoreBackup = {},
                    onListBackupFiles = { emptyList() },
                    onExportCsv = {},
                    getShareIntent = { android.content.Intent() },
                    isBiometricEnabled = false,
                    onSetBiometricEnabled = { toggledBiometric = it }
                )
            }
        }

        // Biometric Security Card exists
        composeTestRule.onNodeWithTag("biometric_security_card").assertExists().assertIsDisplayed()

        // Biometric switch exists
        composeTestRule.onNodeWithTag("biometric_auth_switch").assertExists().performClick()
        composeTestRule.waitForIdle()

        assertEquals(true, toggledBiometric)
    }
}
