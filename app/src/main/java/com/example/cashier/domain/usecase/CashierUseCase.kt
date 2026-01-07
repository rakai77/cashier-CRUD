package com.example.cashier.domain.usecase

import com.example.cashier.domain.model.Cashier
import com.example.cashier.domain.repository.CashierRepository

class GetAllCashierUseCase(private val repository: CashierRepository) {
    operator fun invoke() = repository.getAllCashier()
}

class GetCashierByIdUseCase(private val repository: CashierRepository) {
    operator fun invoke(id: Int) = repository.getCashierById(id)
}

class InsertCashierUseCase(private val repository: CashierRepository) {
    suspend operator fun invoke(cashier: Cashier) = repository.insertCashier(cashier)
}

class UpdateCashierUseCase(private val repository: CashierRepository) {
    suspend operator fun invoke(cashier: Cashier) = repository.updateCashier(cashier)
}

class DeleteCashierUseCase(private val repository: CashierRepository) {
    suspend operator fun invoke(id: Int) = repository.deleteCashier(id)
}

data class CashierUseCases(
    val getAllCashier: GetAllCashierUseCase,
    val getCashierById: GetCashierByIdUseCase,
    val insertCashier: InsertCashierUseCase,
    val updateCashier: UpdateCashierUseCase,
    val deleteCashier: DeleteCashierUseCase
)