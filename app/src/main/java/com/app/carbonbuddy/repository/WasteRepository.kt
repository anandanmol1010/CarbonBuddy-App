package com.app.carbonbuddy.repository

import android.content.Context
import com.app.carbonbuddy.data.CarbonDatabase
import com.app.carbonbuddy.data.WasteDao
import com.app.carbonbuddy.data.WasteEntry
import com.app.carbonbuddy.data.WasteStats
import kotlinx.coroutines.flow.Flow
import java.util.*

class WasteRepository(context: Context) {
    
    private val wasteDao = CarbonDatabase.getDatabase(context).wasteDao()
    
    suspend fun saveWasteEntry(
        inputType: String,
        inputText: String,
        recycledWeight: Double,
        recycledEmission: Double,
        compostedWeight: Double,
        compostedEmission: Double,
        landfillWeight: Double,
        landfillEmission: Double
    ) {
        val entry = WasteEntry.create(
            inputType = inputType,
            inputText = inputText,
            recycledWeight = recycledWeight,
            recycledEmission = recycledEmission,
            compostedWeight = compostedWeight,
            compostedEmission = compostedEmission,
            landfillWeight = landfillWeight,
            landfillEmission = landfillEmission
        )
        wasteDao.insertWasteEntry(entry)
    }
    
    fun getAllWasteEntries(): Flow<List<WasteEntry>> {
        return wasteDao.getAllWasteEntries()
    }
    
    suspend fun getWasteEntriesByDate(date: String): List<WasteEntry> {
        return wasteDao.getWasteEntriesByDate(date)
    }
    
    suspend fun getWasteStats(): WasteStats {
        val currentDate = WasteDao.getCurrentDateString()
        val currentWeek = WasteDao.getCurrentWeek()
        val currentMonth = WasteDao.getCurrentMonth()
        val currentYear = WasteDao.getCurrentYear()
        
        return WasteStats(
            todayNetImpact = wasteDao.getDailyNetImpact(currentDate),
            weeklyNetImpact = wasteDao.getWeeklyNetImpact(currentWeek, currentYear),
            monthlyNetImpact = wasteDao.getMonthlyNetImpact(currentMonth, currentYear),
            todayCount = wasteDao.getDailyCount(currentDate),
            weeklyCount = wasteDao.getWeeklyCount(currentWeek, currentYear),
            monthlyCount = wasteDao.getMonthlyCount(currentMonth, currentYear),
            // Individual category breakdown
            monthlyRecycledWeight = wasteDao.getMonthlyRecycledWeight(currentMonth, currentYear),
            monthlyRecycledEmission = wasteDao.getMonthlyRecycledEmission(currentMonth, currentYear),
            monthlyCompostedWeight = wasteDao.getMonthlyCompostedWeight(currentMonth, currentYear),
            monthlyCompostedEmission = wasteDao.getMonthlyCompostedEmission(currentMonth, currentYear),
            monthlyLandfillWeight = wasteDao.getMonthlyLandfillWeight(currentMonth, currentYear),
            monthlyLandfillEmission = wasteDao.getMonthlyLandfillEmission(currentMonth, currentYear)
        )
    }
    
    fun getDailyNetImpactFlow(date: String): Flow<Double> {
        return wasteDao.getDailyNetImpactFlow(date)
    }
    
    fun getWeeklyNetImpactFlow(week: Int, year: Int): Flow<Double> {
        return wasteDao.getWeeklyNetImpactFlow(week, year)
    }
    
    fun getMonthlyNetImpactFlow(month: Int, year: Int): Flow<Double> {
        return wasteDao.getMonthlyNetImpactFlow(month, year)
    }
    
    suspend fun clearAllWasteEntries() {
        wasteDao.clearAllWasteEntries()
    }
    
    // Helper functions for current date/time
    fun getCurrentDateString(): String = WasteDao.getCurrentDateString()
    fun getCurrentWeek(): Int = WasteDao.getCurrentWeek()
    fun getCurrentMonth(): Int = WasteDao.getCurrentMonth()
    fun getCurrentYear(): Int = WasteDao.getCurrentYear()
}
