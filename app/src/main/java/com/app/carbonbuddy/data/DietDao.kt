package com.app.carbonbuddy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface DietDao {
    @Insert
    suspend fun insertDietEntry(entry: DietEntry)
    
    @Query("SELECT * FROM diet_entries ORDER BY timestamp DESC")
    fun getAllDietEntries(): Flow<List<DietEntry>>
    
    @Query("SELECT * FROM diet_entries WHERE dateString = :date ORDER BY timestamp DESC")
    suspend fun getDietEntriesByDate(date: String): List<DietEntry>
    
    @Query("SELECT * FROM diet_entries WHERE month = :month AND year = :year ORDER BY timestamp DESC")
    suspend fun getDietEntriesByMonth(month: Int, year: Int): List<DietEntry>
    
    @Query("SELECT SUM(totalEmissionGrams) FROM diet_entries WHERE dateString = :date")
    suspend fun getDailyEmissionSum(date: String): Double?
    
    @Query("SELECT SUM(totalEmissionGrams) FROM diet_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyEmissionSum(month: Int, year: Int): Double?
    
    @Query("SELECT SUM(totalEmissionGrams) FROM diet_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyEmissionSum(week: Int, year: Int): Double?
    
    @Query("SELECT COUNT(*) FROM diet_entries WHERE dateString = :date")
    suspend fun getDailyMealCount(date: String): Int
    
    @Query("SELECT COUNT(*) FROM diet_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyMealCount(month: Int, year: Int): Int
    
    @Query("SELECT COUNT(*) FROM diet_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyMealCount(week: Int, year: Int): Int
    
    @Query("SELECT DISTINCT dateString FROM diet_entries WHERE month = :month AND year = :year ORDER BY dayOfMonth")
    suspend fun getActiveDatesInMonth(month: Int, year: Int): List<String>
    
    @Query("DELETE FROM diet_entries")
    suspend fun clearAllDietEntries()
}
