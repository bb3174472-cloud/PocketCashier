package com.pocketcashier.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SuspendedSaleDao {
    @Insert suspend fun insertSuspendedSale(sale: SuspendedSale)
    @Query("SELECT * FROM suspended_sales ORDER BY timestamp DESC") fun getAllSuspended(): Flow<List<SuspendedSale>>
    @Query("DELETE FROM suspended_sales WHERE id = :id") suspend fun deleteSuspendedSale(id: Int)
}
