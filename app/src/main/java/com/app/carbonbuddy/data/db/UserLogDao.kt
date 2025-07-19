package com.app.carbonbuddy.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete

@Dao
interface UserLogDao {
    @Insert
    suspend fun insertLog(log: UserLogEntity)

    @Query("SELECT * FROM user_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<UserLogEntity>

    @Query("SELECT * FROM user_logs WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getLogsByCategory(category: String): List<UserLogEntity>

    @Delete
    suspend fun deleteLog(log: UserLogEntity)
}