package com.app.carbonbuddy.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [TransportEntry::class, DietEntry::class, ShoppingEntry::class, BillsEntry::class, WasteEntry::class],
    version = 6,
    exportSchema = false
)
abstract class CarbonDatabase : RoomDatabase() {
    
    abstract fun transportDao(): TransportDao
    abstract fun dietDao(): DietDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun billsDao(): BillsDao
    abstract fun wasteDao(): WasteDao
    
    companion object {
        @Volatile
        private var INSTANCE: CarbonDatabase? = null
        
        fun getDatabase(context: Context): CarbonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CarbonDatabase::class.java,
                    "carbon_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
