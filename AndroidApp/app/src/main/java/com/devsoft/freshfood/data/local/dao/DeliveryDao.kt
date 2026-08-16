package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devsoft.freshfood.data.local.entity.DeliveryItemEntity
import com.devsoft.freshfood.data.local.entity.DeliveryOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryOrder(order: DeliveryOrderEntity)

    @Update
    suspend fun updateDeliveryOrder(order: DeliveryOrderEntity)

    @Query("SELECT * FROM delivery_orders ORDER BY created_at DESC")
    fun getAllDeliveryOrders(): Flow<List<DeliveryOrderEntity>>

    @Query("SELECT * FROM delivery_orders WHERE id = :id")
    suspend fun getDeliveryOrderById(id: String): DeliveryOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryItems(items: List<DeliveryItemEntity>)

    @Query("SELECT * FROM delivery_items WHERE delivery_order_id = :orderId")
    suspend fun getItemsForDeliveryOrder(orderId: String): List<DeliveryItemEntity>

    @Query("DELETE FROM delivery_items WHERE delivery_order_id = :orderId")
    suspend fun deleteItemsForDeliveryOrder(orderId: String)

    @Query("DELETE FROM delivery_orders WHERE id = :id")
    suspend fun deleteDeliveryOrderById(id: String)
}
