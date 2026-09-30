package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class CategoryExpenseSlice(
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val color: Color,
    val startAngle: Float = 0f,
    val sweepAngle: Float = 0f
)

// Curated high-vibrancy D3 / Recharts categorical palette
private val RECHARTS_D3_PALETTE = listOf(
    Color(0xFFFF6F61), // Coral
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF8B5CF6), // Violet
    Color(0xFFEC4899), // Pink
    Color(0xFF06B6D4), // Cyan
    Color(0xFF84CC16), // Lime
    Color(0xFF6366F1), // Indigo
    Color(0xFFF97316), // Orange
    Color(0xFF14B8A6), // Teal
    Color(0xFFA855F7)  // Purple
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonthlyExpenseChartCard(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    currencySymbol: String,
    totalExpense: Double,
    onCategoryClick: ((String) -> Unit)? = null,
    onLogExpenseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 1. Group expense transactions by category
    val expenseTransactions = remember(transactions) {
        transactions.filter { it.type.equals("EXPENSE", ignoreCase = true) && it.amount > 0.0 }
    }

    val categoryColorMap = remember(categories) {
        categories.associate { cat ->
            val color = try {
                if (cat.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(cat.colorHex))
                else null
            } catch (_: Exception) {
                null
            }
            cat.name to color
        }
    }

    val slices = remember(expenseTransactions, categoryColorMap, totalExpense) {
        val grouped = expenseTransactions.groupBy { it.categoryName }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        val total = if (totalExpense > 0) totalExpense else grouped.sumOf { it.second }
        if (total <= 0.0 || grouped.isEmpty()) {
            emptyList()
        } else {
            var currentAngle = -90f // Start from 12 o'clock
            val paddingDegrees = if (grouped.size > 1) 2.5f else 0f
            val totalPadding = paddingDegrees * grouped.size
            val availableDegrees = (360f - totalPadding).coerceAtLeast(0f)

            grouped.mapIndexed { index, (catName, amount) ->
                val fraction = (amount / total).toFloat()
                val sweep = (fraction * availableDegrees).coerceAtLeast(1.5f)
                val sliceStart = currentAngle
                currentAngle += sweep + paddingDegrees

                val sliceColor = categoryColorMap[catName]
                    ?: RECHARTS_D3_PALETTE[index % RECHARTS_D3_PALETTE.size]

                CategoryExpenseSlice(
                    categoryName = catName,
                    amount = amount,
                    percentage = (amount / total) * 100.0,
                    color = sliceColor,
                    startAngle = sliceStart,
                    sweepAngle = sweep
                )
            }
        }
    }

    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    val selectedSlice = remember(selectedCategoryName, slices) {
        slices.find { it.categoryName == selectedCategoryName }
    }

    // Animation progress for donut draw
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(slices.size, totalExpense) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
        ),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_expenses_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Expense Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Categorical spending distribution",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = if (slices.isNotEmpty()) "${slices.size} Categories" else "0 Categories",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (slices.isEmpty() || totalExpense <= 0.0) {
                // Empty state donut placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 18.dp.toPx()
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.2f),
                            style = Stroke(
                                width = strokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
                            )
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "$currencySymbol 0.00",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "No expenses this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (onLogExpenseClick != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = onLogExpenseClick,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Expense", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                // Circular D3 / Recharts Donut Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val outerDiameter = 180.dp
                    val strokeDp = 24.dp

                    Canvas(
                        modifier = Modifier
                            .size(outerDiameter)
                            .testTag("d3_recharts_donut_canvas")
                            .pointerInput(slices) {
                                detectTapGestures { tapOffset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = tapOffset.x - center.x
                                    val dy = tapOffset.y - center.y
                                    val dist = sqrt(dx * dx + dy * dy)
                                    val outerR = size.width / 2f
                                    val innerR = outerR - 26.dp.toPx()

                                    // If tap is in the donut ring
                                    if (dist in (innerR * 0.7f)..(outerR * 1.2f)) {
                                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                        // Normalize angle from -90° (top)
                                        if (angle < -90f) angle += 360f

                                        // Find which slice covers this angle
                                        val hit = slices.find { slice ->
                                            var start = slice.startAngle
                                            var end = slice.startAngle + slice.sweepAngle
                                            if (start < -90f) start += 360f
                                            if (end < -90f) end += 360f
                                            if (end >= start) {
                                                angle in start..end
                                            } else {
                                                angle >= start || angle <= end
                                            }
                                        }

                                        selectedCategoryName = if (hit != null && selectedCategoryName != hit.categoryName) {
                                            hit.categoryName
                                        } else {
                                            null
                                        }
                                        if (hit != null) {
                                            onCategoryClick?.invoke(hit.categoryName)
                                        }
                                    } else {
                                        // Tap in center clears selection
                                        selectedCategoryName = null
                                    }
                                }
                            }
                    ) {
                        val strokeWidth = strokeDp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(diameter, diameter)

                        // 1. Subtle background track
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.05f),
                            radius = diameter / 2f,
                            style = Stroke(width = strokeWidth)
                        )

                        // 2. Category Segments (Recharts-style arcs with paddingAngle)
                        val progress = animationProgress.value
                        slices.forEach { slice ->
                            val isSelected = selectedCategoryName == slice.categoryName
                            val arcStroke = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth
                            val sliceAlpha = if (selectedCategoryName == null || isSelected) 1.0f else 0.40f

                            drawArc(
                                color = slice.color.copy(alpha = sliceAlpha),
                                startAngle = slice.startAngle,
                                sweepAngle = slice.sweepAngle * progress,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = arcStroke,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    // Donut Hole Central Metric (Interactive D3/Recharts Center Readout)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(118.dp)
                            .clip(CircleShape)
                            .clickable {
                                selectedCategoryName = null
                            }
                            .padding(8.dp)
                            .testTag("donut_center_metric")
                    ) {
                        AnimatedContent(
                            targetState = selectedSlice,
                            label = "DonutCenterTransition"
                        ) { activeSlice ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (activeSlice != null) {
                                    Text(
                                        text = activeSlice.categoryName.uppercase(Locale.US),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = activeSlice.color,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatCurrency(activeSlice.amount, currencySymbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.1f", activeSlice.percentage)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "TOTAL SPENT",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = formatCurrency(totalExpense, currencySymbol),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Tap slice to inspect",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recharts / D3 Legend & Breakdown Chips
                Text(
                    text = "CATEGORY SPENDING",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEach { slice ->
                        val isSelected = selectedCategoryName == slice.categoryName
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) slice.color.copy(alpha = 0.20f)
                            else MaterialTheme.colorScheme.surface,
                            tonalElevation = if (isSelected) 4.dp else 1.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedCategoryName = if (isSelected) null else slice.categoryName
                                    onCategoryClick?.invoke(slice.categoryName)
                                }
                                .testTag("category_chip_${slice.categoryName.lowercase().replace(" ", "_")}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(slice.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = slice.categoryName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", slice.percentage)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${formatCurrency(slice.amount, currencySymbol)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
