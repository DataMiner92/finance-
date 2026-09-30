package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecurityManagerTest {

    private lateinit var context: Context
    private lateinit var securityManager: SecurityManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        securityManager = SecurityManager(context)
    }

    @Test
    fun `app locks on startup with configured PIN`() {
        // App must be locked on startup
        assertTrue("App should be locked on startup", securityManager.isLocked.value)
        assertTrue("PIN should be configured on startup", securityManager.isPinConfigured.value)
    }

    @Test
    fun `verify default PIN unlocks app and incorrect PIN fails`() {
        // Incorrect PIN fails
        val wrongUnlock = securityManager.verifyPin("9999")
        assertFalse("Wrong PIN should return false", wrongUnlock)
        assertTrue("App should remain locked after failed attempt", securityManager.isLocked.value)

        // Default PIN (1234) succeeds
        val correctUnlock = securityManager.verifyPin("1234")
        assertTrue("Correct PIN should return true", correctUnlock)
        assertFalse("App should be unlocked after entering correct PIN", securityManager.isLocked.value)
    }

    @Test
    fun `change PIN updates encrypted store and verifies with new PIN`() {
        // Set new PIN
        val setSuccess = securityManager.setPin("5678", "My Secret Hint")
        assertTrue(setSuccess)
        assertEquals("My Secret Hint", securityManager.getSecurityHint())

        // Lock now
        securityManager.lockNow()
        assertTrue(securityManager.isLocked.value)

        // Old PIN no longer works
        assertFalse(securityManager.verifyPin("1234"))
        assertTrue(securityManager.isLocked.value)

        // New PIN works
        assertTrue(securityManager.verifyPin("5678"))
        assertFalse(securityManager.isLocked.value)
    }

    @Test
    fun `security hint is retrieved correctly`() {
        securityManager.setPin("4321", "Graduation Year")
        assertEquals("Graduation Year", securityManager.getSecurityHint())
    }
}
