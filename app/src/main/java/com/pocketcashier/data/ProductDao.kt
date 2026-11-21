package com.pocketcashier.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(product: Product)

    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    // دالة جديدة لخصم الكمية بعد البيع
    @Query("UPDATE products SET quantity = quantity - :soldQuantity WHERE id = :productId")
    suspend fun updateQuantity(productId: Int, soldQuantity: Int)
}
