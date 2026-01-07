package com.example.cashier.data.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cashier")
data class CashierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "name_input")
    val nameInput: String,
    @ColumnInfo(name = "name_output")
    val nameOutput: String,
    @ColumnInfo(name = "date")
    val date: String,
    @ColumnInfo(name = "time")
    val time: String,
    @ColumnInfo(name = "nominal")
    val nominal: Long,
    @ColumnInfo(name = "struck")
    val struck: String
)