package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCategoryIconVector(iconName: String): ImageVector {
    return when (iconName.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "home", "house", "rent" -> Icons.Default.Home
        "bolt", "electricity", "utility", "bills" -> Icons.Default.Bolt
        "directions_car", "transport", "commute", "car" -> Icons.Default.DirectionsCar
        "local_hospital", "health", "medical" -> Icons.Default.LocalHospital
        "school", "education", "fees" -> Icons.Default.School
        "agriculture", "farm", "crops" -> Icons.Default.Agriculture
        "volunteer_activism", "family", "support", "gift" -> Icons.Default.VolunteerActivism
        "credit_card", "debt", "loan" -> Icons.Default.CreditCard
        "checkroom", "clothing", "personal" -> Icons.Default.Checkroom
        "payments", "salary", "wage" -> Icons.Default.Payments
        "store", "business", "shop" -> Icons.Default.Store
        "yard", "produce", "harvest" -> Icons.Default.Yard
        "smartphone", "mobile", "remittance", "mpesa" -> Icons.Default.Smartphone
        "work", "freelance", "gig" -> Icons.Default.Work
        "savings", "bank" -> Icons.Default.Savings
        "shield", "emergency", "buffer" -> Icons.Default.Shield
        else -> Icons.Default.MoreHoriz
    }
}

fun parseColorHex(hex: String, fallback: Color = Color(0xFF0D7C66)): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorInt = if (cleanHex.length == 6) {
            android.graphics.Color.parseColor("#$cleanHex")
        } else if (cleanHex.length == 8) {
            android.graphics.Color.parseColor("#$cleanHex")
        } else {
            return fallback
        }
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

fun formatCurrency(amount: Double, symbol: String = "$"): String {
    val formatter = DecimalFormat("#,##0.00")
    return "$symbol${formatter.format(amount)}"
}

fun formatDateShort(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateRelative(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDay = 86_400_000L
    return when {
        diff < 0 -> formatDateShort(timestamp)
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000L}m ago"
        diff < oneDay && SimpleDateFormat("yyyyDDD", Locale.US).format(Date(now)) ==
                SimpleDateFormat("yyyyDDD", Locale.US).format(Date(timestamp)) -> "Today"
        diff < 2 * oneDay -> "Yesterday"
        else -> formatDateShort(timestamp)
    }
}

@Composable
fun CategoryAvatar(
    iconName: String,
    colorHex: String,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    val baseColor = parseColorHex(colorHex)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(baseColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getCategoryIconVector(iconName),
            contentDescription = null,
            tint = baseColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun SecurityShieldBadge(
    isEncrypted: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEncrypted) "🛡️ Offline & Encrypted" else "🛡️ Offline Only",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
