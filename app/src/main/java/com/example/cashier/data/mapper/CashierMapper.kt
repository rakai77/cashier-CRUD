package com.example.cashier.data.mapper

import com.example.cashier.data.local.database.entity.CashierEntity
import com.example.cashier.domain.model.Cashier

fun CashierEntity.toModel(): Cashier {
    return Cashier(
        id = id,
        nameInput = nameInput,
        nameOutput = nameOutput,
        date = date,
        time = time,
        nominal = nominal,
        struck = struck
    )
}

fun Cashier.toEntity(): CashierEntity {
    return CashierEntity(
        id = id,
        nameInput = nameInput,
        nameOutput = nameOutput,
        date = date,
        time = time,
        nominal = nominal,
        struck = struck
    )
}
