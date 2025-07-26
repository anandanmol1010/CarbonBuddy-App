package com.app.carbonbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "bills_entries")
data class BillsEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val inputType: String, // "OCR" or "Manual"
    val billType: String, // "ELECTRICITY", "GAS", "WATER", "INTERNET"
    val inputText: String, // Original text (extracted or manual)
    val electricityUnits: Double = 0.0,
    val gasConsumption: Double = 0.0,
    val waterUsage: Double = 0.0,
    val internetData: Double = 0.0,
    val electricityEmission: Double = 0.0,
    val gasEmission: Double = 0.0,
    val waterEmission: Double = 0.0,
    val internetEmission: Double = 0.0,
    val totalEmission: Double = 0.0,
    val ecoTips: String, // JSON string of eco tips list
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = getCurrentDateString(),
    val dayOfMonth: Int = getCurrentDayOfMonth(),
    val month: Int = getCurrentMonth(),
    val year: Int = getCurrentYear(),
    val weekOfYear: Int = getCurrentWeekOfYear()
)

data class BillsStats(
    val todayEmission: Double = 0.0,
    val monthlyEmission: Double = 0.0,
    val weeklyEmission: Double = 0.0,
    val todayBillCount: Int = 0,
    val monthlyBillCount: Int = 0,
    val weeklyBillCount: Int = 0
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
