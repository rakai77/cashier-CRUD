package com.example.cashier.domain.model

data class Cashier(
    val id: Int = 0,
    val nameInput: String,
    val nameOutput: String,
    val date: String,
    val time: String,
    val nominal: Long,
    val struck: String,
    val description: String
)