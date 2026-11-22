package com.pocketcashier.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Insert
    suspend fun insert(supplier: Supplier)

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("UPDATE suppliers SET balance = balance + :amount WHERE id = :supplierId")
    suspend fun updateBalance(supplierId: Int, amount: Double)

    @Query("DELETE FROM suppliers WHERE id = :supplierId")
    suspend fun deleteSupplier(supplierId: Int)
}
