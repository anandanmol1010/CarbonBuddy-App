package com.app.carbonbuddy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface BillsDao {
    @Insert
    suspend fun insertBillsEntry(entry: BillsEntry)
    
    @Query("SELECT * FROM bills_entries ORDER BY timestamp DESC")
    fun getAllBillsEntries(): Flow<List<BillsEntry>>
    
    @Query("SELECT * FROM bills_entries WHERE dateString = :date ORDER BY timestamp DESC")
    suspend fun getBillsEntriesByDate(date: String): List<BillsEntry>
    
    @Query("SELECT * FROM bills_entries WHERE month = :month AND year = :year ORDER BY timestamp DESC")
    suspend fun getBillsEntriesByMonth(month: Int, year: Int): List<BillsEntry>
    
    @Query("SELECT SUM(totalEmission) FROM bills_entries WHERE dateString = :date")
    suspend fun getDailyEmissionSum(date: String): Double?
    
    @Query("SELECT SUM(totalEmission) FROM bills_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyEmissionSum(month: Int, year: Int): Double?
    
    @Query("SELECT SUM(totalEmission) FROM bills_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyEmissionSum(week: Int, year: Int): Double?
    
    @Query("SELECT COUNT(*) FROM bills_entries WHERE dateString = :date")
    suspend fun getDailyBillCount(date: String): Int
    
    @Query("SELECT COUNT(*) FROM bills_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyBillCount(month: Int, year: Int): Int
    
    @Query("SELECT COUNT(*) FROM bills_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyBillCount(week: Int, year: Int): Int
    
    @Query("SELECT DISTINCT dateString FROM bills_entries WHERE month = :month AND year = :year ORDER BY dayOfMonth")
    suspend fun getActiveDatesInMonth(month: Int, year: Int): List<String>
    
    @Query("DELETE FROM bills_entries")
    suspend fun clearAllBillsEntries()
}
