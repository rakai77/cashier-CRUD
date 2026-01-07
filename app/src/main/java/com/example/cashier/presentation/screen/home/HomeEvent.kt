package com.example.cashier.presentation.screen.home

import android.net.Uri

sealed class HomeEvent {
    data class InsertOrUpdate(val id: Int? = null) : HomeEvent()
    data class Delete(val id: Int) : HomeEvent()
    data class LoadCashier(val id: Int) : HomeEvent()
    object ClearForm : HomeEvent()
    object ClearStruckImage : HomeEvent()
    data class OnNameInputChanged(val value: String) : HomeEvent()
    data class OnNameOutputChanged(val value: String) : HomeEvent()
    data class OnDateChanged(val value: String) : HomeEvent()
    data class OnTimeChanged(val value: String) : HomeEvent()
    data class OnNominalChanged(val value: String) : HomeEvent()
    data class OnStruckChanged(val value: Uri) : HomeEvent()
}
