package com.example.ui.screens.security

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import com.example.data.security.BiometricAuthManager
import com.example.data.security.BiometricStatus
import com.example.ui.components.CreateNewPinDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.preferences.AppPreferences
import com.example.ui.components.formatDateShort
import com.example.ui.theme.IncomeGreen
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityBackupScreen(
    isPinConfigured: Boolean,
    isAutoBackupEnabled: Boolean,
    lastBackupTimestamp: Long,
    autoLockTimeoutMinutes: Int,
    currencyCode: String,
    currencySymbol: String,
    themeMode: String,
    backupFolderPath: String,
    onSetupPin: (pin: String, hint: String) -> Boolean,
    onRemovePin: () -> Boolean,
    onLockNow: () -> Unit,
    onSetAutoBackup: (Boolean) -> Unit,
    onSetAutoLockTimeout: (Int) -> Unit,
    onSetCurrency: (symbol: String, code: String) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onPerformManualBackup: () -> Unit,
    onRestoreBackup: (file: File) -> Unit,
    onListBackupFiles: () -> List<File>,
    onExportCsv: ((File) -> Unit) -> Unit,
    getShareIntent: (File) -> Intent,
    isBiometricEnabled: Boolean = false,
    onSetBiometricEnabled: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val biometricStatus = remember(context) {
        BiometricAuthManager.getBiometricStatus(context)
    }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var fileToRestore by remember { mutableStateOf<File?>(null) }

    val localBackupFiles = remember(lastBackupTimestamp) {
        onListBackupFiles()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Security & Local Backups",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Privacy first: your financial data is stored locally with AES-256-GCM encryption.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Security & PIN Protection
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isPinConfigured) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "App Lock (PIN Protection)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isPinConfigured) "PIN enabled & encrypted" else "No PIN set (app is open)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isPinConfigured) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isPinConfigured,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showPinSetupDialog = true
                                } else {
                                    onRemovePin()
                                }
                            },
                            modifier = Modifier.testTag("pin_protection_toggle")
                        )
                    }

                    if (!isPinConfigured) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showPinSetupDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_pin_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New PIN", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isPinConfigured) {
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-Lock Timeout",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            var timeoutExpanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { timeoutExpanded = true }) {
                                    Text(
                                        text = if (autoLockTimeoutMinutes == 0) "Immediate" else "$autoLockTimeoutMinutes min",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                androidx.compose.material3.DropdownMenu(
                                    expanded = timeoutExpanded,
                                    onDismissRequest = { timeoutExpanded = false }
                                ) {
                                    listOf(
                                        0 to "Immediate",
                                        1 to "1 minute",
                                        5 to "5 minutes",
                                        15 to "15 minutes"
                                    ).forEach { (mins, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                onSetAutoLockTimeout(mins)
                                                timeoutExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showPinSetupDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("change_pin_button")
                            ) {
                                Text("Create New PIN")
                            }

                            FilledTonalButton(
                                onClick = onLockNow,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lock_now_button")
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock Now")
                            }
                        }
                    }
                }
            }
        }

        // Section 1.5: Biometric Authentication (Fingerprint & Face Unlock)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biometric_security_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Biometric Authentication",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when (biometricStatus) {
                                        BiometricStatus.AVAILABLE ->
                                            if (isBiometricEnabled) "Fingerprint & Face Unlock active"
                                            else "Use fingerprint or face unlock as an alternative or additional layer to PIN"
                                        BiometricStatus.NOT_ENROLLED -> "No biometrics enrolled in device settings"
                                        BiometricStatus.NO_HARDWARE -> "Biometric hardware not available on device"
                                        BiometricStatus.HW_UNAVAILABLE -> "Biometric sensor currently unavailable"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isBiometricEnabled && biometricStatus == BiometricStatus.AVAILABLE)
                                        IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { checked ->
                                onSetBiometricEnabled(checked)
                            },
                            enabled = isPinConfigured,
                            modifier = Modifier.testTag("biometric_auth_switch")
                        )
                    }

                    if (!isPinConfigured) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "A PIN is required as a primary security credential before enabling biometrics.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Section 2: Automated & Local Backups
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Automated Backups",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Automatically snapshot on changes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = isAutoBackupEnabled,
                            onCheckedChange = onSetAutoBackup,
                            modifier = Modifier.testTag("auto_backup_toggle")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (lastBackupTimestamp > 0) {
                        Text(
                            text = "Last backup: ${formatDateShort(lastBackupTimestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Backup Location Info
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Secure Local Folder",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = backupFolderPath,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPerformManualBackup,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("backup_now_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup Now")
                        }

                        OutlinedButton(
                            onClick = {
                                onExportCsv { file ->
                                    val shareIntent = getShareIntent(file)
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Transactions CSV"))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV")
                        }
                    }
                }
            }
        }

        // Section 3: Available Local Backups
        item {
            Text(
                text = "Available Backups (${localBackupFiles.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (localBackupFiles.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No backup archives created yet.\nTap 'Backup Now' to create one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(localBackupFiles) { file ->
                val sizeKb = (file.length() / 1024).coerceAtLeast(1)
                val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
                val dateStr = sdf.format(Date(file.lastModified()))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "$dateStr • ${sizeKb} KB • 🔒 AES-GCM Encrypted",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val shareIntent = getShareIntent(file)
                                    context.startActivity(Intent.createChooser(shareIntent, "Share SafeLedger Backup"))
                                }
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                            }

                            FilledTonalButton(
                                onClick = { fileToRestore = file },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Restore", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Preferences (Currency & Theme)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "App Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Currency Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showCurrencyDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Currency Format", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text("$currencyCode ($currencySymbol)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Theme Selector Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { showThemeDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Brightness4, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Appearance Theme", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(
                                    when (themeMode) {
                                        "DARK" -> "Dark Mode"
                                        "LIGHT" -> "Light Mode"
                                        else -> "System Default"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text("Change", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // PIN Setup Dialog
    if (showPinSetupDialog) {
        CreateNewPinDialog(
            isExistingPin = isPinConfigured,
            onDismiss = { showPinSetupDialog = false },
            onSavePin = { pin, hint ->
                val ok = onSetupPin(pin, hint)
                if (ok) {
                    showPinSetupDialog = false
                }
            }
        )
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCode = currencyCode,
            onDismiss = { showCurrencyDialog = false },
            onSelect = { symbol, code ->
                onSetCurrency(symbol, code)
                showCurrencyDialog = false
            }
        )
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        ThemeModeDialog(
            currentMode = themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = { mode ->
                onSetThemeMode(mode)
                showThemeDialog = false
            }
        )
    }

    // Restore Confirmation Dialog
    if (fileToRestore != null) {
        AlertDialog(
            onDismissRequest = { fileToRestore = null },
            title = { Text("Restore From Backup") },
            text = {
                Text(
                    "Restoring from '${fileToRestore?.name}' will overwrite your current ledger with the data from this backup archive.\n\nAre you sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        fileToRestore?.let { onRestoreBackup(it) }
                        fileToRestore = null
                    }
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToRestore = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PinSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (pin: String, hint: String) -> Unit
) {
    CreateNewPinDialog(
        isExistingPin = false,
        onDismiss = onDismiss,
        onSavePin = onConfirm
    )
}

@Composable
fun CurrencyPickerDialog(
    currentCode: String,
    onDismiss: () -> Unit,
    onSelect: (symbol: String, code: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Select Local Currency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(AppPreferences.AVAILABLE_CURRENCIES) { opt ->
                        val isSelected = opt.code == currentCode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(opt.symbol, opt.code) }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opt.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
            }
        }
    }
}

@Composable
fun ThemeModeDialog(
    currentMode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val modes = listOf(
        "SYSTEM" to "System Default",
        "LIGHT" to "Light Mode",
        "DARK" to "Dark Mode"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Appearance Theme") },
        text = {
            Column {
                modes.forEach { (mode, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        if (currentMode == mode) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
