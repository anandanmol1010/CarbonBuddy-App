package com.app.carbonbuddy.repository

import android.content.Context
import com.app.carbonbuddy.data.*
import com.app.carbonbuddy.viewmodel.ShoppingItem
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class ShoppingRepository(context: Context) {
    
    private val shoppingDao = CarbonDatabase.getDatabase(context).shoppingDao()
    
    suspend fun saveShoppingEntry(
        inputType: String,
        inputText: String,
        totalEmission: Double,
        items: List<ShoppingItem>,
        ecoTips: List<String>
    ) {
        val itemsJson = JSONArray().apply {
            items.forEach { item ->
                put(JSONObject().apply {
                    put("name", item.name)
                    put("category", item.category)
                    put("co2Emission", item.co2Emission)
                    put("icon", item.icon)
                })
            }
        }.toString()
        
        val ecoTipsJson = JSONArray().apply {
            ecoTips.forEach { put(it) }
        }.toString()
        
        val entry = ShoppingEntry(
            inputType = inputType,
            inputText = inputText,
            totalEmission = totalEmission,
            items = itemsJson,
            ecoTips = ecoTipsJson
        )
        
        shoppingDao.insertShoppingEntry(entry)
    }
    
    fun getAllShoppingEntries(): Flow<List<ShoppingEntry>> {
        return shoppingDao.getAllShoppingEntries()
    }
    
    suspend fun getShoppingEntriesByDate(date: String): List<ShoppingEntry> {
        return shoppingDao.getShoppingEntriesByDate(date)
    }
    
    suspend fun getShoppingEntriesByMonth(month: Int, year: Int): List<ShoppingEntry> {
        return shoppingDao.getShoppingEntriesByMonth(month, year)
    }
    
    suspend fun getShoppingStats(): ShoppingStats {
        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        
        val todayEmission = shoppingDao.getDailyEmissionSum(currentDate) ?: 0.0
        val monthlyEmission = shoppingDao.getMonthlyEmissionSum(currentMonth, currentYear) ?: 0.0
        val weeklyEmission = shoppingDao.getWeeklyEmissionSum(currentWeek, currentYear) ?: 0.0
        
        val todayPurchaseCount = shoppingDao.getDailyPurchaseCount(currentDate)
        val monthlyPurchaseCount = shoppingDao.getMonthlyPurchaseCount(currentMonth, currentYear)
        val weeklyPurchaseCount = shoppingDao.getWeeklyPurchaseCount(currentWeek, currentYear)
        
        return ShoppingStats(
            todayEmission = todayEmission,
            monthlyEmission = monthlyEmission,
            weeklyEmission = weeklyEmission,
            todayPurchaseCount = todayPurchaseCount,
            monthlyPurchaseCount = monthlyPurchaseCount,
            weeklyPurchaseCount = weeklyPurchaseCount
        )
    }
    
    suspend fun getDailyEmissionSum(date: String): Double {
        return shoppingDao.getDailyEmissionSum(date) ?: 0.0
    }
    
    suspend fun getMonthlyEmissionSum(month: Int, year: Int): Double {
        return shoppingDao.getMonthlyEmissionSum(month, year) ?: 0.0
    }
    
    suspend fun getWeeklyEmissionSum(week: Int, year: Int): Double {
        return shoppingDao.getWeeklyEmissionSum(week, year) ?: 0.0
    }
    
    suspend fun getActiveDatesInMonth(month: Int, year: Int): List<String> {
        return shoppingDao.getActiveDatesInMonth(month, year)
    }
    
    suspend fun clearAllData() {
        shoppingDao.clearAllShoppingEntries()
    }
}
