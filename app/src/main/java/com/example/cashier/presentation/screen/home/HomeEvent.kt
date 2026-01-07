package com.example.cashier.presentation.screen.home

sealed class HomeEvent {
    data class InsertOrUpdate(val id: Int? = null) : HomeEvent()
    data class Delete(val id: Int) : HomeEvent()
    data class OnNameInputChanged(val value: String) : HomeEvent()
    data class OnNameOutputChanged(val value: String) : HomeEvent()
    data class OnDateChanged(val value: String) : HomeEvent()
    data class OnTimeChanged(val value: String) : HomeEvent()
    data class OnNominalChanged(val value: String) : HomeEvent()
}