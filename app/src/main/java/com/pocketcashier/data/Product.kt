package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val cost: Double = 0.0,
    val wholesalePrice: Double? = null, // سعر الكرتون
    val conversionFactor: Int = 1, // عدد الحبات في الكرتون (1 يعني لا يوجد كرتون)
    val barcode: String? = null,
    val unit: String = "حبة",
    val expiryDate: String? = null,
    val quantity: Int
)
