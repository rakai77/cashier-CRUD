package com.example.cashier.di

import com.example.cashier.data.local.database.CashierDatabase
import com.example.cashier.domain.repository.CashierRepositoryImpl
import com.example.cashier.domain.repository.CashierRepository
import com.example.cashier.domain.usecase.CashierUseCases
import com.example.cashier.domain.usecase.DeleteCashierUseCase
import com.example.cashier.domain.usecase.GetAllCashierUseCase
import com.example.cashier.domain.usecase.GetCashierByIdUseCase
import com.example.cashier.domain.usecase.InsertCashierUseCase
import com.example.cashier.domain.usecase.UpdateCashierUseCase
import com.example.cashier.presentation.screen.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single {
        CashierDatabase.getDatabase(androidContext())
    }

    single {
        get<CashierDatabase>().cashierDao()
    }

    single<CashierRepository> {
        CashierRepositoryImpl(get())
    }

    single {
        CashierUseCases(
            getAllCashier = GetAllCashierUseCase(get()),
            getCashierById = GetCashierByIdUseCase(get()),
            insertCashier = InsertCashierUseCase(get()),
            updateCashier = UpdateCashierUseCase(get()),
            deleteCashier = DeleteCashierUseCase(get())
        )
    }

    viewModelOf(::HomeViewModel)
}