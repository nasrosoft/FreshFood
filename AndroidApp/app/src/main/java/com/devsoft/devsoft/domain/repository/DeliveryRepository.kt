package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.DeliveryOrder
import com.devsoft.devsoft.domain.model.DeliveryOrderWithDetails
import kotlinx.coroutines.flow.Flow

interface DeliveryRepository {
    suspend fun getDeliveries(): Flow<List<DeliveryOrderWithDetails>>
    suspend fun getDeliveryById(id: String): DeliveryOrderWithDetails?
    suspend fun updateDeliveryStatus(id: String, newStatus: String)
    suspend fun updateDeliveryItemsAndComplete(orderId: String, modifiedQuantities: Map<String, Int>, finalPaymentMethod: String? = null)
    suspend fun deleteDeliveryOrder(id: String)
}
