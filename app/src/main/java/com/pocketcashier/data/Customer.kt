package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val balance: Double = 0.0 // الرصيد (إذا كان موجباً فهو دين عليه، وإذا كان سالباً فهو له)
)
