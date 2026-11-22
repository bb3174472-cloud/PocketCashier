package com.pocketcashier.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Insert suspend fun insertPurchase(purchase: Purchase)
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC") fun getAllPurchases(): Flow<List<Purchase>>
    @Query("SELECT SUM(amount) FROM purchases") fun getTotalPurchases(): Flow<Double?>
}
