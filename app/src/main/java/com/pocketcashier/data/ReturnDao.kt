package com.pocketcashier.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnDao {
    @Insert suspend fun insertReturn(returnDamage: ReturnDamage)
    @Query("SELECT * FROM return_damages ORDER BY timestamp DESC") fun getAllReturns(): Flow<List<ReturnDamage>>
}
