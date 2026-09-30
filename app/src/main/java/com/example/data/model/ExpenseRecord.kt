package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Room Database entity representing an individual Expense Record.
 *
 * Required Schema Fields:
 * - [amount]: The monetary value spent on this expense (Double)
 * - [category]: The category classification (e.g., Food & Groceries, Transport & Fuel, Utilities)
 * - [date]: Timestamp in milliseconds representing the date the expense occurred
 * - [description]: Detailed note or description describing the expense
 *
 * Additional Metadata Fields:
 * - [id]: Auto-generated primary key
 * - [paymentMethod]: The method used to pay (e.g., Cash, Mobile Money, Bank, Card)
 * - [createdAt]: System timestamp when the record was created locally
 */
@JsonClass(generateAdapter = true)
@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["date"]),
        Index(value = ["category"])
    ]
)
data class ExpenseRecord(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "date")
    val date: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String = "Cash",

    @ColumnInfo(name = "createdAt")
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Formats the [date] timestamp into a human-readable display string.
     * Example: "Sep 24, 2026"
     */
    fun formattedDate(pattern: String = "MMM dd, yyyy"): String {
        return try {
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(date))
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(date))
        }
    }

    /**
     * Formats the expense amount with the given currency prefix.
     * Example: "$45.50"
     */
    fun formattedAmount(currencySymbol: String = "$"): String {
        return String.format(Locale.US, "%s%.2f", currencySymbol, amount)
    }
}
