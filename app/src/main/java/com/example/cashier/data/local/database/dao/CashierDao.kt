package com.example.cashier.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cashier.data.local.database.entity.CashierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashierDao {

    @Insert
    suspend fun insert(cashier: CashierEntity)

    @Update
    suspend fun update(cashier: CashierEntity)

    @Query("DELETE FROM cashier WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM cashier ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<CashierEntity>>

    @Query("SELECT * FROM cashier WHERE id = :id")
    fun getById(id: Int): Flow<CashierEntity?>
}