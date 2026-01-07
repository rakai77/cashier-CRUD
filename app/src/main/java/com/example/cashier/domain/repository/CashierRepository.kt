package com.example.cashier.domain.repository

import com.example.cashier.domain.model.Cashier
import com.example.cashier.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface CashierRepository {
    fun getAllCashier(): Flow<Resource<List<Cashier>>>
    fun getCashierById(id: Int): Flow<Resource<Cashier?>>
    suspend fun insertCashier(cashier: Cashier)
    suspend fun updateCashier(cashier: Cashier)
    suspend fun deleteCashier(id: Int)
}