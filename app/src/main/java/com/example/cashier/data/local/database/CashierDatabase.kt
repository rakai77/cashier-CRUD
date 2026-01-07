package com.example.cashier.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cashier.data.local.database.dao.CashierDao
import com.example.cashier.data.local.database.entity.CashierEntity

@Database(entities = [CashierEntity::class], version = 1, exportSchema = false)
abstract class CashierDatabase : RoomDatabase() {

    abstract fun cashierDao(): CashierDao

    companion object {
        @Volatile
        private var INSTANCE: CashierDatabase? = null

        fun getDatabase(context: Context): CashierDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CashierDatabase::class.java,
                    "cashier_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}