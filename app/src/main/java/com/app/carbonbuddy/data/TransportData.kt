package com.app.carbonbuddy.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "transport_entries")
data class TransportEntry(
    @PrimaryKey
    val id: String,
    val transportMode: String,
    val distance: Double,
    val emission: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val dayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
    val month: Int = Calendar.getInstance().get(Calendar.MONTH),
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val weekOfYear: Int = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
)

data class TransportStats(
    val todayEmission: Double = 0.0,
    val monthlyEmission: Double = 0.0,
    val totalTrips: Int = 0
)
