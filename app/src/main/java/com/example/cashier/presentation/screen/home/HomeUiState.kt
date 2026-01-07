package com.example.cashier.presentation.screen.home

import com.example.cashier.domain.model.Cashier

data class HomeUiState(
    val cashierList: List<Cashier> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val nameInput: String = "",
    val nameOutput: String = "",
    val date: String = "",
    val time: String = "",
    val nominal: String = "",
    val struck: String = "" // This will hold the path to the photo
)