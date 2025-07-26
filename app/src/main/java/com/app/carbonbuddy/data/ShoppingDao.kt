package com.app.carbonbuddy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface ShoppingDao {
    @Insert
    suspend fun insertShoppingEntry(entry: ShoppingEntry)
    
    @Query("SELECT * FROM shopping_entries ORDER BY timestamp DESC")
    fun getAllShoppingEntries(): Flow<List<ShoppingEntry>>
    
    @Query("SELECT * FROM shopping_entries WHERE dateString = :date ORDER BY timestamp DESC")
    suspend fun getShoppingEntriesByDate(date: String): List<ShoppingEntry>
    
    @Query("SELECT * FROM shopping_entries WHERE month = :month AND year = :year ORDER BY timestamp DESC")
    suspend fun getShoppingEntriesByMonth(month: Int, year: Int): List<ShoppingEntry>
    
    @Query("SELECT SUM(totalEmission) FROM shopping_entries WHERE dateString = :date")
    suspend fun getDailyEmissionSum(date: String): Double?
    
    @Query("SELECT SUM(totalEmission) FROM shopping_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyEmissionSum(month: Int, year: Int): Double?
    
    @Query("SELECT SUM(totalEmission) FROM shopping_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyEmissionSum(week: Int, year: Int): Double?
    
    @Query("SELECT COUNT(*) FROM shopping_entries WHERE dateString = :date")
    suspend fun getDailyPurchaseCount(date: String): Int
    
    @Query("SELECT COUNT(*) FROM shopping_entries WHERE month = :month AND year = :year")
    suspend fun getMonthlyPurchaseCount(month: Int, year: Int): Int
    
    @Query("SELECT COUNT(*) FROM shopping_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getWeeklyPurchaseCount(week: Int, year: Int): Int
    
    @Query("SELECT DISTINCT dateString FROM shopping_entries WHERE month = :month AND year = :year ORDER BY dayOfMonth")
    suspend fun getActiveDatesInMonth(month: Int, year: Int): List<String>
    
    @Query("DELETE FROM shopping_entries")
    suspend fun clearAllShoppingEntries()
}
