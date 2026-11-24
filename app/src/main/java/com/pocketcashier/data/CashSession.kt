package com.pocketcashier.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cash_sessions")
data class CashSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val openingBalance: Double,
    val expectedCash: Double = 0.0,
    val actualCash: Double? = null,
    val difference: Double? = null,
    val isOpen: Boolean = true
)
