package com.pocketcashier.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CashSessionDao {
    @Insert 
    suspend fun openSession(session: CashSession): Long
    
    @Update 
    suspend fun updateSession(session: CashSession)
    
    @Query("SELECT * FROM cash_sessions WHERE isOpen = 1 LIMIT 1")
    fun getActiveSession(): Flow<CashSession?>
    
    @Query("SELECT * FROM cash_sessions ORDER BY id DESC")
    fun getAllSessions(): Flow<List<CashSession>>
    
    @Query("SELECT SUM(paidAmount) FROM sales WHERE timestamp >= :startTime")
    suspend fun getCashSalesSince(startTime: Long): Double?
    
    @Query("SELECT SUM(amount) FROM expenses WHERE timestamp >= :startTime")
    suspend fun getExpensesSince(startTime: Long): Double?
}
