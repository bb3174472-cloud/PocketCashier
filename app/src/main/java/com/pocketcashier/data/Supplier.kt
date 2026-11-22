package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val balance: Double = 0.0 // الرصيد المستحق للمورد
)
