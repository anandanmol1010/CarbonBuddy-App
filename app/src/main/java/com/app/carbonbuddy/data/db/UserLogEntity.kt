package com.app.carbonbuddy.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_logs")
data class UserLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val category: String,
    val co2Value: Double,
    val details: String? = null,
    val imagePath: String? = null
)