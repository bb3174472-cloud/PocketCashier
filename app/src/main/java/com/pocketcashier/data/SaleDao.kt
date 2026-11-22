package com.pocketcashier.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Int): List<SaleItem>

    // إجمالي المبيعات الكلية
    @Query("SELECT SUM(totalAmount) FROM sales")
    fun getTotalSalesAmount(): Flow<Double?>

    // مبيعات اليوم (بناءً على التوقيت الحالي)
    @Query("SELECT SUM(totalAmount) FROM sales WHERE timestamp >= :startOfDay")
    fun getTodaySalesAmount(startOfDay: Long): Flow<Double?>
}
