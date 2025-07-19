package com.app.carbonbuddy.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserLogEntity::class], version = 1, exportSchema = false)
abstract class CarbonBuddyDatabase : RoomDatabase() {
    abstract fun userLogDao(): UserLogDao
}