package com.pocketcashier.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Insert
    suspend fun insert(customer: Customer)

    @Query("SELECT * FROM customers ORDER BY balance DESC, name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("UPDATE customers SET balance = balance + :amount WHERE id = :customerId")
    suspend fun updateBalance(customerId: Int, amount: Double)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomer(customerId: Int)
}
