package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suspended_sales")
data class SuspendedSale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val cartData: String // سيتم حفظ المنتجات هنا كنص JSON
)
