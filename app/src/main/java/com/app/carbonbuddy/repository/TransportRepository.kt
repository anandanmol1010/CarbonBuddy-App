package com.app.carbonbuddy.repository

import android.content.Context
import com.app.carbonbuddy.data.CarbonDatabase
import com.app.carbonbuddy.data.TransportDao
import com.app.carbonbuddy.data.TransportEntry
import com.app.carbonbuddy.data.TransportStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.*

class TransportRepository(context: Context) {
    
    private val database = CarbonDatabase.getDatabase(context)
    private val transportDao: TransportDao = database.transportDao()
    
    suspend fun saveTransportEntry(entry: TransportEntry) {
        transportDao.insertEntry(entry)
    }
    
    fun getAllEntries(): Flow<List<TransportEntry>> {
        return transportDao.getAllEntries()
    }
    
    suspend fun getAllEntriesList(): List<TransportEntry> {
        return transportDao.getAllEntries().first()
    }
    
    suspend fun getTodayStats(): TransportStats {
        val todayDateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        
        val todayEmission = transportDao.getTotalEmissionForDate(todayDateString) ?: 0.0
        val todayTrips = transportDao.getTripCountForDate(todayDateString)
        val monthlyEmission = getMonthlyEmission()
        
        return TransportStats(
            todayEmission = todayEmission,
            monthlyEmission = monthlyEmission,
            totalTrips = todayTrips
        )
    }
    
    suspend fun getMonthlyEmission(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return transportDao.getTotalEmissionForMonth(currentMonth, currentYear) ?: 0.0
    }
    
    suspend fun clearAllData() {
        transportDao.deleteAllEntries()
    }
    
    // Additional date-specific functions for Home Dashboard
    suspend fun getTodayEntries(): List<TransportEntry> {
        val todayDateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        return transportDao.getEntriesForDate(todayDateString)
    }
    
    suspend fun getMonthlyEntries(): List<TransportEntry> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        return transportDao.getEntriesForMonth(currentMonth, currentYear)
    }
    
    suspend fun getWeeklyEmission(): Double {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        return transportDao.getTotalEmissionForWeek(currentWeek, currentYear) ?: 0.0
    }
    
    suspend fun getActiveDatesThisMonth(): List<String> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        return transportDao.getActiveDatesForMonth(currentMonth, currentYear)
    }
}
