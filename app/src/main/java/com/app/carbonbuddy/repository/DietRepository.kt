package com.app.carbonbuddy.repository

import android.content.Context
import com.app.carbonbuddy.data.*
import com.app.carbonbuddy.viewmodel.DietItem
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class DietRepository(context: Context) {
    
    private val dietDao = CarbonDatabase.getDatabase(context).dietDao()
    
    suspend fun saveDietEntry(
        mealType: String,
        mealDescription: String,
        totalEmissionGrams: Double,
        items: List<DietItem>,
        suggestions: List<String>
    ) {
        val itemsJson = JSONArray().apply {
            items.forEach { item ->
                put(JSONObject().apply {
                    put("name", item.name)
                    put("emissionGrams", item.emissionGrams)
                    put("icon", item.icon)
                })
            }
        }.toString()
        
        val suggestionsJson = JSONArray().apply {
            suggestions.forEach { put(it) }
        }.toString()
        
        val entry = DietEntry(
            mealType = mealType,
            mealDescription = mealDescription,
            totalEmissionGrams = totalEmissionGrams,
            items = itemsJson,
            suggestions = suggestionsJson
        )
        
        dietDao.insertDietEntry(entry)
    }
    
    fun getAllDietEntries(): Flow<List<DietEntry>> {
        return dietDao.getAllDietEntries()
    }
    
    suspend fun getDietEntriesByDate(date: String): List<DietEntry> {
        return dietDao.getDietEntriesByDate(date)
    }
    
    suspend fun getDietEntriesByMonth(month: Int, year: Int): List<DietEntry> {
        return dietDao.getDietEntriesByMonth(month, year)
    }
    
    suspend fun getDietStats(): DietStats {
        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        
        val todayEmission = dietDao.getDailyEmissionSum(currentDate) ?: 0.0
        val monthlyEmission = dietDao.getMonthlyEmissionSum(currentMonth, currentYear) ?: 0.0
        val weeklyEmission = dietDao.getWeeklyEmissionSum(currentWeek, currentYear) ?: 0.0
        
        val todayMealCount = dietDao.getDailyMealCount(currentDate)
        val monthlyMealCount = dietDao.getMonthlyMealCount(currentMonth, currentYear)
        val weeklyMealCount = dietDao.getWeeklyMealCount(currentWeek, currentYear)
        
        return DietStats(
            todayEmission = todayEmission / 1000.0, // Convert grams to kg
            monthlyEmission = monthlyEmission / 1000.0, // Convert grams to kg
            weeklyEmission = weeklyEmission / 1000.0, // Convert grams to kg
            todayMealCount = todayMealCount,
            monthlyMealCount = monthlyMealCount,
            weeklyMealCount = weeklyMealCount
        )
    }
    
    suspend fun getDailyEmissionSum(date: String): Double {
        return (dietDao.getDailyEmissionSum(date) ?: 0.0) / 1000.0 // Convert to kg
    }
    
    suspend fun getMonthlyEmissionSum(month: Int, year: Int): Double {
        return (dietDao.getMonthlyEmissionSum(month, year) ?: 0.0) / 1000.0 // Convert to kg
    }
    
    suspend fun getWeeklyEmissionSum(week: Int, year: Int): Double {
        return (dietDao.getWeeklyEmissionSum(week, year) ?: 0.0) / 1000.0 // Convert to kg
    }
    
    suspend fun getActiveDatesInMonth(month: Int, year: Int): List<String> {
        return dietDao.getActiveDatesInMonth(month, year)
    }
    
    suspend fun clearAllData() {
        dietDao.clearAllDietEntries()
    }
}
