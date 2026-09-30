package com.example.ui.screens.lock

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.fragment.app.FragmentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import com.example.data.security.BiometricAuthManager
import com.example.ui.components.CreateNewPinDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

@Composable
fun AppLockScreen(
    onUnlock: (String) -> Boolean,
    securityHint: String,
    onCreateNewPin: ((pin: String, hint: String) -> Boolean)? = null,
    isBiometricEnabled: Boolean = false,
    onBiometricUnlockSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }
    var showCreatePinDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }

    val isBiometricAvailable = remember(context) {
        BiometricAuthManager.isBiometricAvailable(context)
    }
    val canUseBiometrics = isBiometricEnabled && isBiometricAvailable

    fun launchBiometricPrompt() {
        val activity = context.findFragmentActivity()
        if (activity != null) {
            BiometricAuthManager.promptBiometricUnlock(
                activity = activity,
                title = "Unlock SafeLedger",
                subtitle = "Touch fingerprint sensor or use face unlock",
                negativeButtonText = "Use PIN",
                onSuccess = {
                    onBiometricUnlockSuccess()
                },
                onError = { _, _ ->
                    // Fall back to entering PIN
                },
                onFailed = {
                    // Fingerprint not recognized
                }
            )
        }
    }

    LaunchedEffect(canUseBiometrics) {
        if (canUseBiometrics) {
            launchBiometricPrompt()
        }
    }

    fun vibrateFeedback(isError: Boolean = false) {
        try {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val duration = if (isError) 150L else 35L
                v?.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v?.vibrate(if (isError) 150L else 35L)
            }
        } catch (_: Exception) {}
    }

    fun triggerShake() {
        vibrateFeedback(isError = true)
        coroutineScope.launch {
            shakeOffset.animateTo(20f, animationSpec = tween(50))
            shakeOffset.animateTo(-20f, animationSpec = tween(50))
            shakeOffset.animateTo(10f, animationSpec = tween(50))
            shakeOffset.animateTo(-10f, animationSpec = tween(50))
            shakeOffset.animateTo(0f, animationSpec = tween(50))
        }
    }

    fun attemptUnlock(pin: String) {
        val success = onUnlock(pin)
        if (success) {
            showError = false
        } else {
            showError = true
            triggerShake()
            enteredPin = ""
        }
    }

    fun onKeyPressed(digit: String) {
        vibrateFeedback(isError = false)
        if (enteredPin.length < 6) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            showError = false

            // Check if matches immediately upon 4 or 6 digits
            if (newPin.length == 4) {
                val success = onUnlock(newPin)
                if (success) {
                    showError = false
                    return
                }
            } else if (newPin.length == 6) {
                attemptUnlock(newPin)
            }
        }
    }

    fun onDeletePressed() {
        vibrateFeedback(isError = false)
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            showError = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .testTag("app_lock_screen")
        ) {
            // Safe Icon & Branding
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(76.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "SafeLedger Vault Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "SafeLedger Vault",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "App locked for privacy. Enter your PIN to decrypt records.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // PIN Indicator Dots with shake animation
            val dotCount = if (enteredPin.length > 4) 6 else 4
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .testTag("pin_dots_container")
            ) {
                for (i in 0 until dotCount) {
                    val filled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    showError -> MaterialTheme.colorScheme.error
                                    filled -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            AnimatedVisibility(visible = showError) {
                Text(
                    text = "Incorrect PIN. Please try again.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .testTag("pin_error_message")
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Numeric Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            when (key) {
                                "DEL" -> {
                                    IconButton(
                                        onClick = { onDeletePressed() },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("pin_delete_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                "C" -> {
                                    TextButton(
                                        onClick = {
                                            enteredPin = ""
                                            showError = false
                                        },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("pin_clear_button")
                                    ) {
                                        Text(
                                            text = "Clear",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                else -> {
                                    FilledTonalButton(
                                        onClick = { onKeyPressed(key) },
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .testTag("pin_key_$key")
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (canUseBiometrics) {
                OutlinedButton(
                    onClick = { launchBiometricPrompt() },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(48.dp)
                        .testTag("biometric_unlock_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Biometric Unlock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unlock with Biometrics",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Unlock Submit Button
            Button(
                onClick = { attemptUnlock(enteredPin) },
                enabled = enteredPin.length >= 4,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(48.dp)
                    .testTag("unlock_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Unlock SafeLedger",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Security Hint Button
            if (securityHint.isNotBlank()) {
                TextButton(
                    onClick = { showHintDialog = true },
                    modifier = Modifier.testTag("pin_hint_button")
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Forgot PIN? Show Security Hint",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (onCreateNewPin != null) {
                TextButton(
                    onClick = { showCreatePinDialog = true },
                    modifier = Modifier.testTag("lock_create_new_pin_button")
                ) {
                    Icon(Icons.Default.Password, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create New PIN / Reset",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Encryption technology badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "🔒 EncryptedSharedPreferences (AES-256-GCM / PBKDF2)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }

    if (showHintDialog) {
        AlertDialog(
            onDismissRequest = { showHintDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Security Reminder Hint")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your password hint configured during setup:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"$securityHint\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Row {
                    if (onCreateNewPin != null) {
                        TextButton(
                            onClick = {
                                showHintDialog = false
                                showCreatePinDialog = true
                            }
                        ) {
                            Text("Create New PIN")
                        }
                    }
                    TextButton(onClick = { showHintDialog = false }) {
                        Text("Got It")
                    }
                }
            }
        )
    }

    if (showCreatePinDialog && onCreateNewPin != null) {
        CreateNewPinDialog(
            isExistingPin = true,
            onDismiss = { showCreatePinDialog = false },
            onSavePin = { newPin, hint ->
                val success = onCreateNewPin(newPin, hint)
                if (success) {
                    showCreatePinDialog = false
                    enteredPin = ""
                    attemptUnlock(newPin)
                }
            }
        )
    }
}
