package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devsoft.freshfood.data.local.entity.StockBatchEntity
import com.devsoft.freshfood.data.local.entity.StockMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockBatch(batch: StockBatchEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockBatches(batches: List<StockBatchEntity>)

    @Query("SELECT * FROM stock_batches WHERE product_id = :productId AND quantity > 0 ORDER BY expiration_date ASC")
    suspend fun getAvailableBatchesForProduct(productId: String): List<StockBatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovements(movements: List<StockMovementEntity>)
}
