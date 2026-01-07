package com.example.cashier

import android.app.Application
import com.example.cashier.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class CashierApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@CashierApp)
            modules(appModule)
        }
    }
}