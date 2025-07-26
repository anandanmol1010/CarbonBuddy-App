package com.app.carbonbuddy.repository

import android.content.Context
import com.app.carbonbuddy.data.*
import com.app.carbonbuddy.viewmodel.BillData
import com.app.carbonbuddy.viewmodel.EmissionResult
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.util.*

class BillsRepository(context: Context) {
    
    private val billsDao = CarbonDatabase.getDatabase(context).billsDao()
    
    suspend fun saveBillsEntry(
        inputType: String,
        billType: String,
        inputText: String,
        billData: BillData,
        emissionResult: EmissionResult,
        ecoTips: List<String>
    ) {
        val ecoTipsJson = JSONArray().apply {
            ecoTips.forEach { put(it) }
        }.toString()
        
        val entry = BillsEntry(
            inputType = inputType,
            billType = billType,
            inputText = inputText,
            electricityUnits = billData.electricityUnits,
            gasConsumption = billData.gasConsumption,
            waterUsage = billData.waterUsage,
            internetData = billData.internetData,
            electricityEmission = emissionResult.electricityEmission,
            gasEmission = emissionResult.gasEmission,
            waterEmission = emissionResult.waterEmission,
            internetEmission = emissionResult.internetEmission,
            totalEmission = emissionResult.totalEmission,
            ecoTips = ecoTipsJson
        )
        
        billsDao.insertBillsEntry(entry)
    }
    
    fun getAllBillsEntries(): Flow<List<BillsEntry>> {
        return billsDao.getAllBillsEntries()
    }
    
    suspend fun getBillsEntriesByDate(date: String): List<BillsEntry> {
        return billsDao.getBillsEntriesByDate(date)
    }
    
    suspend fun getBillsEntriesByMonth(month: Int, year: Int): List<BillsEntry> {
        return billsDao.getBillsEntriesByMonth(month, year)
    }
    
    suspend fun getBillsStats(): BillsStats {
        val calendar = Calendar.getInstance()
        val currentDate = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        
        val todayEmission = billsDao.getDailyEmissionSum(currentDate) ?: 0.0
        val monthlyEmission = billsDao.getMonthlyEmissionSum(currentMonth, currentYear) ?: 0.0
        val weeklyEmission = billsDao.getWeeklyEmissionSum(currentWeek, currentYear) ?: 0.0
        
        val todayBillCount = billsDao.getDailyBillCount(currentDate)
        val monthlyBillCount = billsDao.getMonthlyBillCount(currentMonth, currentYear)
        val weeklyBillCount = billsDao.getWeeklyBillCount(currentWeek, currentYear)
        
        return BillsStats(
            todayEmission = todayEmission,
            monthlyEmission = monthlyEmission,
            weeklyEmission = weeklyEmission,
            todayBillCount = todayBillCount,
            monthlyBillCount = monthlyBillCount,
            weeklyBillCount = weeklyBillCount
        )
    }
    
    suspend fun getDailyEmissionSum(date: String): Double {
        return billsDao.getDailyEmissionSum(date) ?: 0.0
    }
    
    suspend fun getMonthlyEmissionSum(month: Int, year: Int): Double {
        return billsDao.getMonthlyEmissionSum(month, year) ?: 0.0
    }
    
    suspend fun getWeeklyEmissionSum(week: Int, year: Int): Double {
        return billsDao.getWeeklyEmissionSum(week, year) ?: 0.0
    }
    
    suspend fun getActiveDatesInMonth(month: Int, year: Int): List<String> {
        return billsDao.getActiveDatesInMonth(month, year)
    }
    
    suspend fun clearAllData() {
        billsDao.clearAllBillsEntries()
    }
}
