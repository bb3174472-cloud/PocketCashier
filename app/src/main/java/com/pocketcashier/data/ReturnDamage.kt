package com.pocketcashier.data
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "return_damages")
data class ReturnDamage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val quantity: Int,
    val type: String, // "مرتجع" أو "تالف"
    val timestamp: Long = System.currentTimeMillis()
)
