package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Data item definition for Speedometer radial menu entries.
 */
data class SpeedometerMenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val contentDescription: String,
    val badge: String? = null,
    val accentColor: Color? = null,
    val testTag: String,
    val secondaryTestTag: String? = null,
    val onClick: () -> Unit
)

/**
 * Speedometer Menu Overlay:
 * Draws the expanded speedometer gauge dial, animated needle, tick marks,
 * and radial menu action items anchored to the bottom center.
 */
@Composable
fun SpeedometerMenuOverlay(
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    items: List<SpeedometerMenuItem>,
    modifier: Modifier = Modifier,
    arcRadius: Dp = 138.dp,
    bottomPadding: Dp = 80.dp
) {
    if (!isExpanded) return

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Handle back button to collapse speedometer menu
    BackHandler(enabled = isExpanded) {
        onDismiss()
    }

    val expansionProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 380f
        ),
        label = "SpeedometerOverlayExpansion"
    )

    val needleAngleAnim = remember { Animatable(90f) }
    var hoveredItemTitle by remember { mutableStateOf("Quick Navigation Dial") }

    val startAngle = 165f
    val endAngle = 15f
    val arcSpan = startAngle - endAngle

    val itemCount = items.size
    val angles = remember(itemCount) {
        if (itemCount <= 1) {
            listOf(90f)
        } else {
            List(itemCount) { index ->
                startAngle - (index.toFloat() / (itemCount - 1).toFloat()) * arcSpan
            }
        }
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            needleAngleAnim.snapTo(180f)
            needleAngleAnim.animateTo(
                targetValue = 90f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f)
            )
            hoveredItemTitle = "Select an action"
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val radiusPx = with(density) { arcRadius.toPx() } * expansionProgress

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Semi-transparent scrim as a background sibling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.58f * expansionProgress))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onDismiss()
                }
                .testTag("speedometer_menu_scrim")
        )

        // Radial Speedometer Gauge Content (sibling)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Speedometer Gauge Arc & Needle Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("speedometer_gauge_canvas")
            ) {
                val centerX = size.width / 2f
                val centerY = size.height

                val currentRadius = radiusPx

                // 1. Outer Soft Track Arc
                drawArc(
                    color = primaryColor.copy(alpha = 0.22f * expansionProgress),
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(centerX - currentRadius, centerY - currentRadius),
                    size = androidx.compose.ui.geometry.Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = 22.dp.toPx() * expansionProgress, cap = StrokeCap.Round)
                )

                // 2. High-speed Gradient Arc Track
                val gradientBrush = Brush.sweepGradient(
                    0.50f to primaryColor.copy(alpha = 0.85f * expansionProgress),
                    0.72f to tertiaryColor.copy(alpha = 0.95f * expansionProgress),
                    0.95f to Color(0xFF2E7D32).copy(alpha = 0.90f * expansionProgress),
                    center = Offset(centerX, centerY)
                )

                drawArc(
                    brush = gradientBrush,
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(centerX - currentRadius, centerY - currentRadius),
                    size = androidx.compose.ui.geometry.Size(currentRadius * 2, currentRadius * 2),
                    style = Stroke(width = 5.dp.toPx() * expansionProgress, cap = StrokeCap.Round)
                )

                // 3. Calibrated Speedometer Ticks (Gauge notches)
                val totalTicks = 24
                for (i in 0..totalTicks) {
                    val tickAngle = startAngle - (i.toFloat() / totalTicks.toFloat()) * arcSpan
                    val isMajor = i % 4 == 0
                    val tickLength = (if (isMajor) 14.dp else 7.dp).toPx() * expansionProgress
                    val tickWidth = (if (isMajor) 2.5.dp else 1.2.dp).toPx()

                    val rad = tickAngle * (PI / 180f).toFloat()
                    val outerR = currentRadius + 18.dp.toPx()
                    val innerR = outerR - tickLength

                    val startX = centerX + innerR * cos(rad)
                    val startY = centerY - innerR * sin(rad)
                    val endX = centerX + outerR * cos(rad)
                    val endY = centerY - outerR * sin(rad)

                    drawLine(
                        color = if (isMajor) primaryColor.copy(alpha = 0.9f * expansionProgress)
                        else onSurfaceVariant.copy(alpha = 0.5f * expansionProgress),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = tickWidth,
                        cap = StrokeCap.Round
                    )
                }

                // 4. Sweeping Speedometer Needle
                val needleRad = needleAngleAnim.value * (PI / 180f).toFloat()
                val needleLen = (currentRadius * 0.70f).coerceAtLeast(30f)
                val needleTipX = centerX + needleLen * cos(needleRad)
                val needleTipY = centerY - needleLen * sin(needleRad)

                val perpAngle = needleRad + (PI / 2f).toFloat()
                val needleBaseWidth = 6.dp.toPx() * expansionProgress
                val baseLeftX = centerX + needleBaseWidth * cos(perpAngle)
                val baseLeftY = centerY - needleBaseWidth * sin(perpAngle)
                val baseRightX = centerX - needleBaseWidth * cos(perpAngle)
                val baseRightY = centerY - needleBaseWidth * sin(perpAngle)

                val needlePath = Path().apply {
                    moveTo(baseLeftX, baseLeftY)
                    lineTo(needleTipX, needleTipY)
                    lineTo(baseRightX, baseRightY)
                    close()
                }

                drawPath(
                    path = needlePath,
                    color = Color(0xFFFFB951).copy(alpha = 0.95f * expansionProgress)
                )

                // 5. Center Dial Hub Cap
                drawCircle(
                    color = surfaceColor,
                    radius = 16.dp.toPx() * expansionProgress,
                    center = Offset(centerX, centerY)
                )
                drawCircle(
                    color = primaryColor,
                    radius = 8.dp.toPx() * expansionProgress,
                    center = Offset(centerX, centerY)
                )
            }

            // Speedometer Readout Header Pill
            Box(
                modifier = Modifier
                    .offset(y = (-arcRadius - 62.dp) * expansionProgress)
                    .scale(expansionProgress)
                    .testTag("speedometer_readout_card")
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "QUICK ACTIONS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = hoveredItemTitle,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Radial Menu Items distributed along the arc
            items.forEachIndexed { index, item ->
                val angleDeg = angles.getOrElse(index) { 90f }
                val angleRad = angleDeg * (PI / 180f).toFloat()

                val offsetX = (radiusPx * cos(angleRad)).roundToInt()
                val offsetY = (-radiusPx * sin(angleRad)).roundToInt()

                val itemColor = item.accentColor ?: MaterialTheme.colorScheme.primaryContainer
                val contentColor = if (item.accentColor != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer

                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX, offsetY) }
                        .scale(expansionProgress)
                        .testTag(item.testTag)
                        .then(
                            if (item.secondaryTestTag != null) Modifier.testTag(item.secondaryTestTag) else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Circular Action Node
                        Surface(
                            shape = CircleShape,
                            color = itemColor,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable {
                                    item.onClick()
                                    onDismiss()
                                }
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription,
                                    tint = contentColor,
                                    modifier = Modifier.size(24.dp)
                                )

                                item.badge?.let { badgeText ->
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-2).dp, y = 2.dp)
                                            .background(MaterialTheme.colorScheme.error, CircleShape)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = MaterialTheme.colorScheme.onError,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        // Label Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            shadowElevation = 3.dp
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Speedometer Menu Widget:
 * Standalone floating trigger button with Speedometer gauge icon
 * and integrated radial overlay.
 */
@Composable
fun SpeedometerMenuWidget(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    items: List<SpeedometerMenuItem>,
    modifier: Modifier = Modifier,
    arcRadius: Dp = 138.dp,
    triggerButtonSize: Dp = 56.dp
) {
    val triggerRotation by animateFloatAsState(
        targetValue = if (isExpanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 400f
        ),
        label = "SpeedometerTriggerRotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        // Overlay when expanded
        SpeedometerMenuOverlay(
            isExpanded = isExpanded,
            onDismiss = onToggleExpand,
            items = items,
            arcRadius = arcRadius,
            bottomPadding = triggerButtonSize + 16.dp
        )

        // Central Speedometer Trigger Hub Button
        FloatingActionButton(
            onClick = onToggleExpand,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 10.dp
            ),
            shape = CircleShape,
            modifier = Modifier
                .size(triggerButtonSize)
                .rotate(triggerRotation)
                .shadow(8.dp, CircleShape)
                .testTag("speedometer_menu_trigger")
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Speed,
                contentDescription = if (isExpanded) "Close Actions Menu" else "Quick Actions",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
