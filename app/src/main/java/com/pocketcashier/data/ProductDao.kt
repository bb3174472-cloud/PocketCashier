package com.pocketcashier.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert suspend fun insert(product: Product)
    @Update suspend fun update(product: Product)
    @Delete suspend fun delete(product: Product)
    @Query("SELECT * FROM products ORDER BY id DESC") fun getAllProducts(): Flow<List<Product>>
    @Query("UPDATE products SET quantity = quantity - :soldQuantity WHERE id = :productId") suspend fun updateQuantity(productId: Int, soldQuantity: Int)
    
    // دالة جديدة لتعديل الكمية بعد الجرد (بالزيادة أو النقصان)
    @Query("UPDATE products SET quantity = quantity + :difference WHERE id = :productId") 
    suspend fun updateQuantityDifference(productId: Int, difference: Int)
}
