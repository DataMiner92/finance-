package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun CreateNewPinDialog(
    isExistingPin: Boolean = false,
    onDismiss: () -> Unit,
    onSavePin: (pin: String, hint: String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }
    var showPinText by remember { mutableStateOf(false) }
    var showConfirmPinText by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isPinLengthValid = pin.length in 4..6
    val isMatching = pin.isNotEmpty() && pin == confirmPin
    val canSubmit = isPinLengthValid && isMatching

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isExistingPin) "Change / Create New PIN" else "Create New PIN",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("create_pin_dialog_title")
                        )
                        Text(
                            text = "Set up a numeric PIN to lock your finances",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Encryption Explainer Note
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔒 Your PIN will be hashed using PBKDF2 (12,000 rounds) and stored securely in EncryptedSharedPreferences.",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New PIN Field
                OutlinedTextField(
                    value = pin,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            pin = input
                            errorMessage = null
                        }
                    },
                    label = { Text("New 4-6 Digit PIN") },
                    placeholder = { Text("Enter 4 to 6 numbers") },
                    visualTransformation = if (showPinText) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(
                            onClick = { showPinText = !showPinText },
                            modifier = Modifier.testTag("toggle_new_pin_visibility")
                        ) {
                            Icon(
                                imageVector = if (showPinText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPinText) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    supportingText = {
                        Text(
                            text = "${pin.length}/6 digits (min 4 digits)",
                            color = if (pin.length in 4..6) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_pin_input")
                        .testTag("pin_setup_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Confirm PIN Field
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            confirmPin = input
                            errorMessage = null
                        }
                    },
                    label = { Text("Confirm New PIN") },
                    placeholder = { Text("Re-enter your new PIN") },
                    visualTransformation = if (showConfirmPinText) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isMatching) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "PINs match",
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            IconButton(
                                onClick = { showConfirmPinText = !showConfirmPinText },
                                modifier = Modifier.testTag("toggle_confirm_pin_visibility")
                            ) {
                                Icon(
                                    imageVector = if (showConfirmPinText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPinText) "Hide confirm PIN" else "Show confirm PIN"
                                )
                            }
                        }
                    },
                    supportingText = {
                        if (confirmPin.isNotEmpty()) {
                            if (isMatching) {
                                Text("✓ PINs match", color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                            } else {
                                Text("✗ PINs do not match", color = ExpenseRed)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_pin_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Security Hint Field
                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Security Reminder Hint (Optional)") },
                    placeholder = { Text("e.g. Grandma's old house number") },
                    supportingText = {
                        Text("Displayed if you ever forget your PIN to help you recall it.")
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("security_hint_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_pin_button")
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            if (pin.length < 4) {
                                errorMessage = "PIN must be at least 4 digits."
                            } else if (pin != confirmPin) {
                                errorMessage = "PINs do not match."
                            } else {
                                onSavePin(pin, hint.trim())
                            }
                        },
                        enabled = canSubmit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("store_pin_button")
                            .testTag("save_pin_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Store PIN for Future Access",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
