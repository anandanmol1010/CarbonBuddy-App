package com.app.carbonbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "diet_entries")
data class DietEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealType: String,
    val mealDescription: String,
    val totalEmissionGrams: Double,
    val items: String, // JSON string of DietItem list
    val suggestions: String, // JSON string of suggestions list
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = getCurrentDateString(),
    val dayOfMonth: Int = getCurrentDayOfMonth(),
    val month: Int = getCurrentMonth(),
    val year: Int = getCurrentYear(),
    val weekOfYear: Int = getCurrentWeekOfYear()
)

data class DietStats(
    val todayEmission: Double = 0.0,
    val monthlyEmission: Double = 0.0,
    val weeklyEmission: Double = 0.0,
    val todayMealCount: Int = 0,
    val monthlyMealCount: Int = 0,
    val weeklyMealCount: Int = 0
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
