package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val discount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentType: String = "نقدي", // نقدي، آجل، جزئي
    val customerId: Int? = null,
    val customerName: String? = null
)
