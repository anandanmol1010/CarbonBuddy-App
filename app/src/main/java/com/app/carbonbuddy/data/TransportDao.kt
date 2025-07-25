package com.app.carbonbuddy.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportDao {
    
    @Query("SELECT * FROM transport_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<TransportEntry>>
    
    @Query("SELECT * FROM transport_entries WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getEntriesFromDate(startTime: Long): Flow<List<TransportEntry>>
    
    @Query("SELECT SUM(emission) FROM transport_entries WHERE timestamp >= :startTime")
    suspend fun getTotalEmissionFromDate(startTime: Long): Double?
    
    @Query("SELECT COUNT(*) FROM transport_entries WHERE timestamp >= :startTime")
    suspend fun getTripCountFromDate(startTime: Long): Int
    
    // Date-specific queries for better performance
    @Query("SELECT SUM(emission) FROM transport_entries WHERE dateString = :dateString")
    suspend fun getTotalEmissionForDate(dateString: String): Double?
    
    @Query("SELECT COUNT(*) FROM transport_entries WHERE dateString = :dateString")
    suspend fun getTripCountForDate(dateString: String): Int
    
    @Query("SELECT SUM(emission) FROM transport_entries WHERE month = :month AND year = :year")
    suspend fun getTotalEmissionForMonth(month: Int, year: Int): Double?
    
    @Query("SELECT COUNT(*) FROM transport_entries WHERE month = :month AND year = :year")
    suspend fun getTripCountForMonth(month: Int, year: Int): Int
    
    @Query("SELECT SUM(emission) FROM transport_entries WHERE weekOfYear = :week AND year = :year")
    suspend fun getTotalEmissionForWeek(week: Int, year: Int): Double?
    
    @Query("SELECT * FROM transport_entries WHERE dateString = :dateString ORDER BY timestamp DESC")
    suspend fun getEntriesForDate(dateString: String): List<TransportEntry>
    
    @Query("SELECT * FROM transport_entries WHERE month = :month AND year = :year ORDER BY timestamp DESC")
    suspend fun getEntriesForMonth(month: Int, year: Int): List<TransportEntry>
    
    @Query("SELECT DISTINCT dateString FROM transport_entries WHERE month = :month AND year = :year ORDER BY dateString DESC")
    suspend fun getActiveDatesForMonth(month: Int, year: Int): List<String>
    
    @Query("SELECT * FROM transport_entries WHERE id = :id")
    suspend fun getEntryById(id: String): TransportEntry?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TransportEntry)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<TransportEntry>)
    
    @Update
    suspend fun updateEntry(entry: TransportEntry)
    
    @Delete
    suspend fun deleteEntry(entry: TransportEntry)
    
    @Query("DELETE FROM transport_entries")
    suspend fun deleteAllEntries()
    
    @Query("DELETE FROM transport_entries WHERE timestamp < :beforeTime")
    suspend fun deleteEntriesBeforeDate(beforeTime: Long)
}
