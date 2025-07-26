package com.app.carbonbuddy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface WasteDao {
    
    @Insert
    suspend fun insertWasteEntry(entry: WasteEntry)
    
    @Query("SELECT * FROM waste_entries ORDER BY timestamp DESC")
    fun getAllWasteEntries(): Flow<List<WasteEntry>>
    
    @Query("SELECT * FROM waste_entries WHERE dateString = :date ORDER BY timestamp DESC")
    suspend fun getWasteEntriesByDate(date: String): List<WasteEntry>
    
    @Query("SELECT COALESCE(SUM(netImpact), 0.0) FROM waste_entries WHERE dateString = :date")
    suspend fun getDailyNetImpact(date: String): Double
    
    @Query("SELECT COALESCE(SUM(netImpact), 0.0) FROM waste_entries WHERE dateString = :date")
    fun getDailyNetImpactFlow(date: String): Flow<Double>
    
    @Query("SELECT COALESCE(SUM(netImpact), 0.0) FROM waste_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyNetImpact(week: Int, year: Int): Double
    
    @Query("SELECT COALESCE(SUM(netImpact), 0.0) FROM waste_entries WHERE weekOfYear = :week AND year = :year")
    fun getWeeklyNetImpactFlow(week: Int, year: Int): Flow<Double>
    
    @Query("SELECT COALESCE(SUM(netImpact), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyNetImpact(month: Int, year: Int): Double
    
    @Query("SELECT COALESCE(SUM(netImpact), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    fun getMonthlyNetImpactFlow(month: Int, year: Int): Flow<Double>
    
    @Query("SELECT COUNT(*) FROM waste_entries WHERE dateString = :date")
    suspend fun getDailyCount(date: String): Int
    
    @Query("SELECT COUNT(*) FROM waste_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyCount(week: Int, year: Int): Int
    
    @Query("SELECT COUNT(*) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyCount(month: Int, year: Int): Int
    
    @Query("DELETE FROM waste_entries")
    suspend fun clearAllWasteEntries()
    
    // Individual category queries for monthly data
    @Query("SELECT COALESCE(SUM(recycledWeight), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyRecycledWeight(month: Int, year: Int): Double
    
    @Query("SELECT COALESCE(SUM(recycledEmission), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyRecycledEmission(month: Int, year: Int): Double
    
    @Query("SELECT COALESCE(SUM(compostedWeight), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyCompostedWeight(month: Int, year: Int): Double
    
    @Query("SELECT COALESCE(SUM(compostedEmission), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyCompostedEmission(month: Int, year: Int): Double
    
    @Query("SELECT COALESCE(SUM(landfillWeight), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyLandfillWeight(month: Int, year: Int): Double
    
    @Query("SELECT COALESCE(SUM(landfillEmission), 0.0) FROM waste_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyLandfillEmission(month: Int, year: Int): Double
    
    // Helper functions for current date calculations
    companion object {
        fun getCurrentDateString(): String {
            val calendar = Calendar.getInstance()
            return "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
        }
        
        fun getCurrentWeek(): Int {
            return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        }
        
        fun getCurrentMonth(): Int {
            return Calendar.getInstance().get(Calendar.MONTH) + 1
        }
        
        fun getCurrentYear(): Int {
            return Calendar.getInstance().get(Calendar.YEAR)
        }
    }
}
