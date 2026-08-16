package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.domain.model.DeliveryOrder
import com.devsoft.freshfood.domain.model.DeliveryOrderWithDetails
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    suspend fun getDeliveries(): Flow<List<DeliveryOrderWithDetails>>
    suspend fun getDeliveryById(id: String): DeliveryOrderWithDetails?
    suspend fun updateDeliveryStatus(id: String, newStatus: String)
    suspend fun updateDeliveryItemsAndComplete(orderId: String, modifiedQuantities: Map<String, Int>)
    suspend fun deleteDeliveryOrder(id: String)
}
