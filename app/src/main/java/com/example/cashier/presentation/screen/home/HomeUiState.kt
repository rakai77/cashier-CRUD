package com.example.cashier.presentation.screen.home

import com.example.cashier.domain.model.Cashier

data class HomeUiState(
    val cashierList: List<Cashier> = emptyList(),
    val cashier: Cashier? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val nameInput: String = "",
    val nameInputError: String? = null,
    val nameOutput: String = "",
    val nameOutputError: String? = null,
    val date: String = "",
    val dateError: String? = null,
    val time: String = "",
    val timeError: String? = null,
    val nominal: String = "",
    val nominalError: String? = null,
    val description: String = "",
    val struck: String = ""
)
