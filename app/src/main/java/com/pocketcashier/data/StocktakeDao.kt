package com.pocketcashier.data
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StocktakeDao {
    @Insert suspend fun insertStocktake(stocktake: Stocktake)
    @Query("SELECT * FROM stocktakes ORDER BY timestamp DESC") fun getAllStocktakes(): Flow<List<Stocktake>>
}
