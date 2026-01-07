package com.example.cashier.domain.repository

import com.example.cashier.data.local.database.dao.CashierDao
import com.example.cashier.data.mapper.toEntity
import com.example.cashier.data.mapper.toModel
import com.example.cashier.domain.model.Cashier
import com.example.cashier.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

class CashierRepositoryImpl(private val cashierDao: CashierDao) : CashierRepository {
    override fun getAllCashier(): Flow<Resource<List<Cashier>>> = flow {
        emit(Resource.Loading())
        try {
            cashierDao.getAll().collect { entities ->
                emit(Resource.Success(entities.map { it.toModel() }))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Could not load data from database"))
        }
    }

    override fun getCashierById(id: Int): Flow<Resource<Cashier?>> = flow {
        emit(Resource.Loading())
        try {
            cashierDao.getById(id).collect { entity ->
                if (entity != null) {
                    emit(Resource.Success(entity.toModel()))
                } else {
                    emit(Resource.Error("Item not found"))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Could not load data from database"))
        }
    }

    override suspend fun insertCashier(cashier: Cashier) {
        cashierDao.insert(cashier.toEntity())
    }

    override suspend fun updateCashier(cashier: Cashier) {
        cashierDao.update(cashier.toEntity())
    }

    override suspend fun deleteCashier(id: Int) {
        cashierDao.delete(id)
    }
}