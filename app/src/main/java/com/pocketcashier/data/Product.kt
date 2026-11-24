package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val cost: Double = 0.0,
    val barcode: String? = null,
    val unit: String = "حبة", // الوحدة (حبة، كرتون، كجم)
    val expiryDate: String? = null, // تاريخ الانتهاء (اختياري)
    val quantity: Int
)
