package com.app.carbonbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "shopping_entries")
data class ShoppingEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inputType: String, // "OCR" or "Manual"
    val inputText: String, // Original text (extracted or manual)
    val totalEmission: Double, // in kg CO₂
    val items: String, // JSON string of ShoppingItem list
    val ecoTips: String, // JSON string of eco tips list
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = getCurrentDateString(),
    val dayOfMonth: Int = getCurrentDayOfMonth(),
    val month: Int = getCurrentMonth(),
    val year: Int = getCurrentYear(),
    val weekOfYear: Int = getCurrentWeekOfYear()
)

data class ShoppingStats(
    val todayEmission: Double = 0.0,
    val monthlyEmission: Double = 0.0,
    val weeklyEmission: Double = 0.0,
    val todayPurchaseCount: Int = 0,
    val monthlyPurchaseCount: Int = 0,
    val weeklyPurchaseCount: Int = 0
)

// Helper functions for date calculations
private fun getCurrentDateString(): String {
    val calendar = Calendar.getInstance()
    return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
}

private fun getCurrentDayOfMonth(): Int {
    return Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
}

private fun getCurrentMonth(): Int {
    return Calendar.getInstance().get(Calendar.MONTH) + 1
}

private fun getCurrentYear(): Int {
    return Calendar.getInstance().get(Calendar.YEAR)
}

private fun getCurrentWeekOfYear(): Int {
    return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
}
