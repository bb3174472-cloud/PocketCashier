package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stocktakes")
data class Stocktake(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val productName: String,
    val systemQuantity: Int,
    val actualQuantity: Int,
    val difference: Int
)
