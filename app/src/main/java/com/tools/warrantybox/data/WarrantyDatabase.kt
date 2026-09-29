package com.tools.warrantybox.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WarrantyItem::class], version = 1, exportSchema = false)
abstract class WarrantyDatabase : RoomDatabase() {
    abstract fun warrantyDao(): WarrantyDao

    companion object {
        @Volatile
        private var INSTANCE: WarrantyDatabase? = null

        fun getDatabase(context: Context): WarrantyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarrantyDatabase::class.java,
                    "warranty_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
