package com.example.ui.screens.expenses

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.BudgetProgress
import com.example.ui.components.CategoryAvatar
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDateRelative
import com.example.ui.screens.transactions.CreateCustomCategoryDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogExpenseScreen(
    categories: List<CategoryEntity>,
    budgetProgressList: List<BudgetProgress>,
    recentTransactions: List<TransactionEntity>,
    currencySymbol: String,
    onSaveExpense: (amount: Double, category: String, description: String, paymentMethod: String, timestamp: Long) -> Unit,
    onAddNewCategory: (name: String, iconName: String, colorHex: String, type: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Form States
    var amountInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var timestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val expenseCategories = remember(categories) {
        categories.filter { it.type == "EXPENSE" || it.type == "BOTH" }
    }

    var selectedCategory by remember(expenseCategories) {
        mutableStateOf(expenseCategories.firstOrNull()?.name ?: "Food & Groceries")
    }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var paymentDropdownExpanded by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val paymentOptions = listOf(
        "Cash",
        "Mobile Money",
        "Bank Transfer",
        "Credit Card",
        "Debit Card",
        "Other"
    )

    // Today's expenses filter
    val todayTransactions = remember(recentTransactions) {
        val calNow = Calendar.getInstance()
        val nowDay = calNow.get(Calendar.DAY_OF_YEAR)
        val nowYear = calNow.get(Calendar.YEAR)
        val calItem = Calendar.getInstance()

        recentTransactions.filter { item ->
            if (item.type != "EXPENSE") return@filter false
            calItem.timeInMillis = item.timestamp
            calItem.get(Calendar.DAY_OF_YEAR) == nowDay && calItem.get(Calendar.YEAR) == nowYear
        }
    }

    val todayTotalSpent = remember(todayTransactions) {
        todayTransactions.sumOf { it.amount }
    }

    // Budget impact computation
    val activeBudget = remember(budgetProgressList, selectedCategory) {
        budgetProgressList.find { it.budget.categoryName.equals(selectedCategory, ignoreCase = true) }
    }

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0
    val isAmountValid = parsedAmount > 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Log Daily Expense",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.testTag("log_expense_title")
                    )
                    Text(
                        text = "Track your daily spend to keep budgets healthy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Daily Summary Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ExpenseRed.copy(alpha = 0.12f),
                    modifier = Modifier.testTag("today_spend_pill")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Today's Total",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ExpenseRed
                        )
                        Text(
                            text = formatCurrency(todayTotalSpent, currencySymbol),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        // Expense Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("log_expense_form_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Amount Field
                    Text(
                        text = "1. Enter Amount",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                                amountInput = input
                            }
                        },
                        label = { Text("Expense Amount") },
                        placeholder = { Text("0.00") },
                        leadingIcon = {
                            Text(
                                text = currencySymbol,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_amount_input")
                    )

                    // Quick Amount Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(5, 10, 25, 50, 100).forEach { quickVal ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val cur = amountInput.toDoubleOrNull() ?: 0.0
                                        amountInput = String.format(Locale.US, "%.2f", cur + quickVal)
                                    }
                            ) {
                                Text(
                                    text = "+$quickVal",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Category Dropdown Menu
                    Text(
                        text = "2. Expense Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val selectedCatObj = expenseCategories.find { it.name.equals(selectedCategory, ignoreCase = true) }

                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Category") },
                            leadingIcon = {
                                if (selectedCatObj != null) {
                                    CategoryAvatar(
                                        iconName = selectedCatObj.iconName,
                                        colorHex = selectedCatObj.colorHex,
                                        size = 32.dp,
                                        iconSize = 16.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Category, contentDescription = null)
                                }
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("expense_category_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.testTag("category_dropdown_menu")
                        ) {
                            expenseCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CategoryAvatar(
                                                iconName = cat.iconName,
                                                colorHex = cat.colorHex,
                                                size = 28.dp,
                                                iconSize = 14.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = cat.name,
                                                fontWeight = if (cat.name == selectedCategory) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    trailingIcon = if (cat.name == selectedCategory) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    } else null,
                                    onClick = {
                                        selectedCategory = cat.name
                                        categoryDropdownExpanded = false
                                    },
                                    modifier = Modifier.testTag("category_option_${cat.name}")
                                )
                            }

                            HorizontalDivider()

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("+ Create New Category", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                },
                                onClick = {
                                    categoryDropdownExpanded = false
                                    showNewCategoryDialog = true
                                }
                            )
                        }
                    }

                    // Description / Title Text Field
                    Text(
                        text = "3. Description / Item Title",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("What was this expense for?") },
                        placeholder = { Text("e.g. Lunch with team, Groceries, Electricity token") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_description_input")
                    )

                    // Payment Method Dropdown Menu
                    Text(
                        text = "4. Payment Method",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    ExposedDropdownMenuBox(
                        expanded = paymentDropdownExpanded,
                        onExpandedChange = { paymentDropdownExpanded = !paymentDropdownExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedPaymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Paid Via") },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (selectedPaymentMethod) {
                                        "Cash" -> Icons.Default.Money
                                        "Mobile Money" -> Icons.Default.PhoneAndroid
                                        "Credit Card", "Debit Card" -> Icons.Default.CreditCard
                                        else -> Icons.Default.Payment
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDropdownExpanded)
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("expense_payment_method_dropdown")
                        )

                        ExposedDropdownMenu(
                            expanded = paymentDropdownExpanded,
                            onDismissRequest = { paymentDropdownExpanded = false }
                        ) {
                            paymentOptions.forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(method) },
                                    trailingIcon = if (selectedPaymentMethod == method) {
                                        { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    } else null,
                                    onClick = {
                                        selectedPaymentMethod = method
                                        paymentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Date Selector Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val oneDay = 86_400_000L
                        val now = System.currentTimeMillis()
                        FilterChip(
                            selected = (now - timestamp) < oneDay / 2,
                            onClick = { timestamp = now },
                            label = { Text("Today") },
                            leadingIcon = if ((now - timestamp) < oneDay / 2) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = (now - timestamp) in (oneDay / 2)..(3 * oneDay / 2),
                            onClick = { timestamp = now - oneDay },
                            label = { Text("Yesterday") },
                            leadingIcon = if ((now - timestamp) in (oneDay / 2)..(3 * oneDay / 2)) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }

                    // Notes / Tags Text Field
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Optional Notes or Location") },
                        placeholder = { Text("e.g. Downtown Farmers Market") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_notes_input")
                    )

                    // Real-Time Budget Impact Card
                    if (activeBudget != null) {
                        val projectedSpent = activeBudget.spentAmount + parsedAmount
                        val projectedFraction = (projectedSpent / activeBudget.budget.monthlyLimit).toFloat().coerceIn(0f, 1.5f)
                        val willExceed = projectedSpent > activeBudget.budget.monthlyLimit

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                willExceed -> ExpenseRed.copy(alpha = 0.1f)
                                projectedFraction >= 0.8f -> WarningAmber.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (willExceed) Icons.Default.Warning else Icons.Default.Info,
                                            contentDescription = null,
                                            tint = if (willExceed) ExpenseRed else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Monthly Budget Impact: ${activeBudget.budget.categoryName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "${(projectedFraction * 100).toInt()}% of limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (willExceed) ExpenseRed else MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { projectedFraction.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = when {
                                        willExceed -> ExpenseRed
                                        projectedFraction >= 0.8f -> WarningAmber
                                        else -> IncomeGreen
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                val remainingAfter = activeBudget.budget.monthlyLimit - projectedSpent
                                Text(
                                    text = if (remainingAfter >= 0) {
                                        "Remaining after logging: ${formatCurrency(remainingAfter, currencySymbol)}"
                                    } else {
                                        "⚠️ Exceeds limit by ${formatCurrency(-remainingAfter, currencySymbol)}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = if (remainingAfter >= 0) MaterialTheme.colorScheme.onSurfaceVariant else ExpenseRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Submit Action Button
                    Button(
                        onClick = {
                            if (isAmountValid && selectedCategory.isNotBlank()) {
                                val fullNote = buildString {
                                    if (descriptionInput.isNotBlank()) append(descriptionInput.trim())
                                    if (notesInput.isNotBlank()) {
                                        if (isNotEmpty()) append(" • ")
                                        append(notesInput.trim())
                                    }
                                }

                                onSaveExpense(
                                    parsedAmount,
                                    selectedCategory,
                                    fullNote,
                                    selectedPaymentMethod,
                                    timestamp
                                )

                                // Reset inputs for fast sequential entry
                                amountInput = ""
                                descriptionInput = ""
                                notesInput = ""
                            }
                        },
                        enabled = isAmountValid && selectedCategory.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("log_expense_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ExpenseRed
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAmountValid) "Log ${formatCurrency(parsedAmount, currencySymbol)} Expense" else "Log Daily Expense",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Today's Logged Expenses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expenses Logged Today (${todayTransactions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (todayTransactions.isNotEmpty()) {
                    Text(
                        text = "Total: ${formatCurrency(todayTotalSpent, currencySymbol)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
            }
        }

        if (todayTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses logged today yet.\nFill the form above and tap 'Log Daily Expense'!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(todayTransactions, key = { it.id }) { item ->
                val cat = categories.find { it.name.equals(item.categoryName, ignoreCase = true) }
                val iconName = cat?.iconName ?: "shopping_cart"
                val colorHex = cat?.colorHex ?: "#0D7C66"

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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            CategoryAvatar(
                                iconName = iconName,
                                colorHex = colorHex,
                                size = 42.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (item.note.isNotBlank()) item.note else item.paymentMethod,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "- " + formatCurrency(item.amount, currencySymbol),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = formatDateRelative(item.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showNewCategoryDialog) {
        CreateCustomCategoryDialog(
            type = "EXPENSE",
            onDismiss = { showNewCategoryDialog = false },
            onConfirm = { name, icon, colorHex ->
                onAddNewCategory(name, icon, colorHex, "EXPENSE")
                selectedCategory = name
                showNewCategoryDialog = false
            }
        )
    }
}
