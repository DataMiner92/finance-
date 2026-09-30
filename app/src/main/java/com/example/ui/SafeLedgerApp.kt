package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SpeedometerMenuItem
import com.example.ui.components.SpeedometerMenuOverlay
import com.example.ui.screens.budgets.BudgetsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.expenses.LogExpenseScreen
import com.example.ui.screens.lock.AppLockScreen
import com.example.ui.screens.savings.SavingsGoalsScreen
import com.example.ui.screens.security.SecurityBackupScreen
import com.example.ui.screens.tips.FinancialTipsScreen
import com.example.ui.screens.transactions.AddEditTransactionDialog
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.theme.SafeLedgerTheme

enum class SafeLedgerTab(val label: String) {
    DASHBOARD("Home"),
    LOG_EXPENSE("+ Expense"),
    TRANSACTIONS("Ledger"),
    BUDGETS("Budgets"),
    SAVINGS("Savings"),
    TIPS("Tips"),
    SECURITY("Security")
}

@Composable
fun SafeLedgerApp(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val isSystemDark = isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemDark
    }

    SafeLedgerTheme(darkTheme = isDarkTheme) {
        val isLocked by viewModel.isLocked.collectAsStateWithLifecycle()
        val isPinConfigured by viewModel.isPinConfigured.collectAsStateWithLifecycle()
        val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
        val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
        val currencyCode by viewModel.currencyCode.collectAsStateWithLifecycle()
        val isAutoBackupEnabled by viewModel.isAutoBackupEnabled.collectAsStateWithLifecycle()
        val lastBackupTimestamp by viewModel.lastBackupTimestamp.collectAsStateWithLifecycle()
        val autoLockTimeoutMinutes by viewModel.autoLockTimeoutMinutes.collectAsStateWithLifecycle()

        val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
        val monthSummary by viewModel.monthSummary.collectAsStateWithLifecycle()
        val filteredTransactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
        val allCategories by viewModel.allCategories.collectAsStateWithLifecycle()
        val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
        val allSavingsGoals by viewModel.allSavingsGoals.collectAsStateWithLifecycle()

        val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }

        var currentTab by remember { mutableStateOf(SafeLedgerTab.DASHBOARD) }
        var showAddTransactionDialog by remember { mutableStateOf(false) }
        var isSpeedometerExpanded by remember { mutableStateOf(false) }

        // Speedometer Menu items that collapse actions & secondary navigation
        val speedometerItems = remember {
            listOf(
                SpeedometerMenuItem(
                    id = "log_expense",
                    title = "+ Expense",
                    subtitle = "Quick Entry",
                    icon = Icons.Default.AddCircle,
                    contentDescription = "Log Expense",
                    accentColor = Color(0xFFE76F51),
                    testTag = "speedometer_item_log_expense",
                    secondaryTestTag = "tab_log_expense",
                    onClick = {
                        viewModel.onUserInteraction()
                        currentTab = SafeLedgerTab.LOG_EXPENSE
                    }
                ),
                SpeedometerMenuItem(
                    id = "add_transaction",
                    title = "+ Entry",
                    subtitle = "Income & Other",
                    icon = Icons.Default.Add,
                    contentDescription = "Add Transaction",
                    accentColor = Color(0xFF006C50),
                    testTag = "speedometer_item_add_transaction",
                    onClick = {
                        viewModel.onUserInteraction()
                        showAddTransactionDialog = true
                    }
                ),
                SpeedometerMenuItem(
                    id = "savings",
                    title = "Savings",
                    subtitle = "Goals & Targets",
                    icon = Icons.Default.Savings,
                    contentDescription = "Savings Goals",
                    accentColor = Color(0xFF845400),
                    testTag = "speedometer_item_savings",
                    secondaryTestTag = "tab_savings",
                    onClick = {
                        viewModel.onUserInteraction()
                        currentTab = SafeLedgerTab.SAVINGS
                    }
                ),
                SpeedometerMenuItem(
                    id = "budgets",
                    title = "Budgets",
                    subtitle = "Limits & Alerts",
                    icon = Icons.Default.PieChart,
                    contentDescription = "Budgets",
                    accentColor = Color(0xFF41B3A2),
                    testTag = "speedometer_item_budgets",
                    onClick = {
                        viewModel.onUserInteraction()
                        currentTab = SafeLedgerTab.BUDGETS
                    }
                ),
                SpeedometerMenuItem(
                    id = "tips",
                    title = "Tips",
                    subtitle = "Financial Insights",
                    icon = Icons.Default.Lightbulb,
                    contentDescription = "Financial Tips",
                    accentColor = Color(0xFFFFB951),
                    testTag = "speedometer_item_tips",
                    secondaryTestTag = "tab_tips",
                    onClick = {
                        viewModel.onUserInteraction()
                        currentTab = SafeLedgerTab.TIPS
                    }
                ),
                SpeedometerMenuItem(
                    id = "security",
                    title = "Security",
                    subtitle = "PIN & Backup",
                    icon = Icons.Default.Security,
                    contentDescription = "Security and Backup",
                    accentColor = Color(0xFF354B40),
                    testTag = "speedometer_item_security",
                    onClick = {
                        viewModel.onUserInteraction()
                        currentTab = SafeLedgerTab.SECURITY
                    }
                )
            )
        }

        // Back navigation handling
        BackHandler(enabled = isSpeedometerExpanded) {
            isSpeedometerExpanded = false
        }
        BackHandler(enabled = !isSpeedometerExpanded && currentTab != SafeLedgerTab.DASHBOARD) {
            currentTab = SafeLedgerTab.DASHBOARD
        }

        // Show status snackbar
        LaunchedEffect(statusMessage) {
            statusMessage?.let { msg ->
                snackbarHostState.showSnackbar(msg)
                viewModel.clearStatusMessage()
            }
        }

        // Auto-lock check on resume/tick
        LaunchedEffect(Unit) {
            viewModel.checkAutoLock()
        }

        if (isLocked) {
            AppLockScreen(
                onUnlock = { pin -> viewModel.unlockApp(pin) },
                securityHint = viewModel.getSecurityHint(),
                onCreateNewPin = { pin, hint -> viewModel.setupPin(pin, hint) },
                isBiometricEnabled = isBiometricEnabled,
                onBiometricUnlockSuccess = { viewModel.unlockWithBiometrics() },
                modifier = modifier
            )
        } else {
            Box(modifier = modifier.fillMaxSize()) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp,
                            modifier = Modifier.testTag("bottom_navigation_bar")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == SafeLedgerTab.DASHBOARD,
                                onClick = {
                                    viewModel.onUserInteraction()
                                    currentTab = SafeLedgerTab.DASHBOARD
                                },
                                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Home") },
                                label = { Text("Home") },
                                modifier = Modifier.testTag("tab_home")
                            )
                            NavigationBarItem(
                                selected = currentTab == SafeLedgerTab.TRANSACTIONS,
                                onClick = {
                                    viewModel.onUserInteraction()
                                    currentTab = SafeLedgerTab.TRANSACTIONS
                                },
                                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Ledger") },
                                label = { Text("Ledger") },
                                modifier = Modifier.testTag("tab_transactions")
                            )

                            // Center Speedometer Menu Widget Trigger Button
                            val speedometerRotation by animateFloatAsState(
                                targetValue = if (isSpeedometerExpanded) 135f else 0f,
                                animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
                                label = "BottomBarSpeedometerRotation"
                            )
                            val isSpeedometerActiveTab = currentTab in listOf(
                                SafeLedgerTab.LOG_EXPENSE,
                                SafeLedgerTab.SAVINGS,
                                SafeLedgerTab.TIPS
                            )

                            NavigationBarItem(
                                selected = isSpeedometerExpanded || isSpeedometerActiveTab,
                                onClick = {
                                    viewModel.onUserInteraction()
                                    isSpeedometerExpanded = !isSpeedometerExpanded
                                },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(
                                                        MaterialTheme.colorScheme.primary,
                                                        MaterialTheme.colorScheme.tertiary
                                                    )
                                                )
                                            )
                                            .rotate(speedometerRotation),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeedometerExpanded) Icons.Default.Close else Icons.Default.Speed,
                                            contentDescription = "Quick Actions",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (isSpeedometerActiveTab && !isSpeedometerExpanded) currentTab.label else "Actions",
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                },
                                modifier = Modifier.testTag("speedometer_menu_trigger")
                            )

                            NavigationBarItem(
                                selected = currentTab == SafeLedgerTab.BUDGETS,
                                onClick = {
                                    viewModel.onUserInteraction()
                                    currentTab = SafeLedgerTab.BUDGETS
                                },
                                icon = { Icon(Icons.Default.PieChart, contentDescription = "Budgets") },
                                label = { Text("Budgets") },
                                modifier = Modifier.testTag("tab_budgets")
                            )
                            NavigationBarItem(
                                selected = currentTab == SafeLedgerTab.SECURITY,
                                onClick = {
                                    viewModel.onUserInteraction()
                                    currentTab = SafeLedgerTab.SECURITY
                                },
                                icon = { Icon(Icons.Default.Security, contentDescription = "Security") },
                                label = { Text("Security") },
                                modifier = Modifier.testTag("tab_security")
                            )
                        }
                    }
                ) { innerPadding ->
                Crossfade(
                    targetState = currentTab,
                    modifier = Modifier.padding(innerPadding),
                    label = "TabContent"
                ) { tab ->
                    when (tab) {
                        SafeLedgerTab.DASHBOARD -> {
                            DashboardScreen(
                                monthSummary = monthSummary,
                                recentTransactions = filteredTransactions,
                                categories = allCategories,
                                budgetProgressList = budgetProgressList,
                                selectedMonth = selectedMonth,
                                currencySymbol = currencySymbol,
                                isPinConfigured = isPinConfigured,
                                onNavigateToTransactions = { currentTab = SafeLedgerTab.TRANSACTIONS },
                                onNavigateToBudgets = { currentTab = SafeLedgerTab.BUDGETS },
                                onNavigateToSavings = { currentTab = SafeLedgerTab.SAVINGS },
                                onNavigateToTips = { currentTab = SafeLedgerTab.TIPS },
                                onNavigateToSecurity = { currentTab = SafeLedgerTab.SECURITY },
                                onAddTransactionClick = { showAddTransactionDialog = true },
                                onNavigateToLogExpense = { currentTab = SafeLedgerTab.LOG_EXPENSE },
                                onLockClick = { viewModel.lockNow() },
                                onOpenSpeedometerMenu = { isSpeedometerExpanded = true },
                                onMonthChange = { newMonth -> viewModel.setSelectedMonth(newMonth) }
                            )
                        }
                        SafeLedgerTab.LOG_EXPENSE -> {
                            LogExpenseScreen(
                                categories = allCategories,
                                budgetProgressList = budgetProgressList,
                                recentTransactions = filteredTransactions,
                                currencySymbol = currencySymbol,
                                onSaveExpense = { amount, category, description, paymentMethod, timestamp ->
                                    viewModel.addTransaction(
                                        type = "EXPENSE",
                                        amount = amount,
                                        categoryName = category,
                                        timestamp = timestamp,
                                        note = description,
                                        paymentMethod = paymentMethod,
                                        goalId = null
                                    )
                                },
                                onAddNewCategory = { name, icon, col, type ->
                                    viewModel.addCategory(name, icon, col, type)
                                }
                            )
                        }
                        SafeLedgerTab.TRANSACTIONS -> {
                            TransactionsScreen(
                                transactions = filteredTransactions,
                                categories = allCategories,
                                currencySymbol = currencySymbol,
                                onAddTransactionClick = { showAddTransactionDialog = true },
                                onDeleteTransaction = { t -> viewModel.deleteTransaction(t) }
                            )
                        }
                        SafeLedgerTab.BUDGETS -> {
                            BudgetsScreen(
                                budgetProgressList = budgetProgressList,
                                categories = allCategories,
                                currencySymbol = currencySymbol,
                                onSaveBudget = { cat, limit -> viewModel.saveBudget(cat, limit) },
                                onDeleteBudget = { b -> viewModel.deleteBudget(b) }
                            )
                        }
                        SafeLedgerTab.SAVINGS -> {
                            SavingsGoalsScreen(
                                savingsGoals = allSavingsGoals,
                                currencySymbol = currencySymbol,
                                onAddGoal = { name, target, cur, targetDate, icon, col, notes ->
                                    viewModel.addSavingsGoal(name, target, cur, targetDate, icon, col, notes)
                                },
                                onUpdateGoalAmount = { goal, delta, isDeposit ->
                                    viewModel.updateGoalAmount(goal, delta, isDeposit)
                                },
                                onDeleteGoal = { g -> viewModel.deleteSavingsGoal(g) }
                            )
                        }
                        SafeLedgerTab.TIPS -> {
                            FinancialTipsScreen(
                                currencySymbol = currencySymbol
                            )
                        }
                        SafeLedgerTab.SECURITY -> {
                            SecurityBackupScreen(
                                isPinConfigured = isPinConfigured,
                                isAutoBackupEnabled = isAutoBackupEnabled,
                                lastBackupTimestamp = lastBackupTimestamp,
                                autoLockTimeoutMinutes = autoLockTimeoutMinutes,
                                currencyCode = currencyCode,
                                currencySymbol = currencySymbol,
                                themeMode = themeMode,
                                backupFolderPath = viewModel.getBackupFolderDirectory(),
                                onSetupPin = { pin, hint -> viewModel.setupPin(pin, hint) },
                                onRemovePin = { viewModel.removePin() },
                                onLockNow = { viewModel.lockNow() },
                                onSetAutoBackup = { en -> viewModel.setAutoBackup(en) },
                                onSetAutoLockTimeout = { mins -> viewModel.setAutoLockTimeout(mins) },
                                onSetCurrency = { sym, code -> viewModel.setCurrency(sym, code) },
                                onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                onPerformManualBackup = { viewModel.performManualBackup() },
                                onRestoreBackup = { file -> viewModel.restoreBackup(file) },
                                onListBackupFiles = { viewModel.listBackupFiles() },
                                onExportCsv = { cb -> viewModel.exportCsv(cb) },
                                getShareIntent = { file -> viewModel.getShareIntent(file) },
                                isBiometricEnabled = isBiometricEnabled,
                                onSetBiometricEnabled = { en -> viewModel.setBiometricEnabled(en) }
                            )
                        }
                    }
                }
            }

            if (showAddTransactionDialog) {
                AddEditTransactionDialog(
                    categories = allCategories,
                    savingsGoals = allSavingsGoals,
                    currencySymbol = currencySymbol,
                    onDismiss = { showAddTransactionDialog = false },
                    onSave = { type, amount, category, timestamp, note, paymentMethod, goalId ->
                        viewModel.addTransaction(type, amount, category, timestamp, note, paymentMethod, goalId)
                    },
                    onAddNewCategory = { name, icon, col, type ->
                        viewModel.addCategory(name, icon, col, type)
                    }
                )
            }

            // Speedometer Radial Gauge overlay
            SpeedometerMenuOverlay(
                isExpanded = isSpeedometerExpanded,
                onDismiss = { isSpeedometerExpanded = false },
                items = speedometerItems,
                bottomPadding = 86.dp
            )
        }
    }
}
}
