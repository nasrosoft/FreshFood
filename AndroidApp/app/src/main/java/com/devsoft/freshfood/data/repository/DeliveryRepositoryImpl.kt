package com.devsoft.freshfood.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.devsoft.freshfood.data.local.FreshFoodDatabase
import com.devsoft.freshfood.data.local.entity.SyncQueueEntity
import com.devsoft.freshfood.domain.model.*
import com.devsoft.freshfood.domain.repository.DeliveryRepository
import com.devsoft.freshfood.utils.DeviceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DeliveryRepositoryImpl(
    private val database: FreshFoodDatabase,
    private val context: Context
) : DeliveryRepository {

    override suspend fun getDeliveries(): Flow<List<DeliveryOrderWithDetails>> {
        return database.deliveryDao().getAllDeliveryOrders().map { entities ->
            entities.map { enrichDeliveryOrder(it.toDomainModel()) }
        }
    }

    override suspend fun getDeliveryById(id: String): DeliveryOrderWithDetails? {
        val order = database.deliveryDao().getDeliveryOrderById(id)?.toDomainModel()
        return order?.let { enrichDeliveryOrder(it) }
    }

    override suspend fun updateDeliveryStatus(id: String, newStatus: String) {
        val deviceId = DeviceUtil.getDeviceId(context)
        
        database.withTransaction {
            val order = database.deliveryDao().getDeliveryOrderById(id)
            if (order != null) {
                database.deliveryDao().updateDeliveryOrder(order.copy(status = newStatus))
                
                // Enqueue sync operation
                val updatePayload = mapOf("id" to id, "status" to newStatus)
                database.syncQueueDao().insert(
                    SyncQueueEntity(
                        entity_type = "delivery_orders",
                        entity_id = id,
                        operation = "UPDATE",
                        payload = Json.encodeToString(updatePayload),
                        device_id = deviceId
                    )
                )
            }
        }
    }

    private suspend fun enrichDeliveryOrder(order: DeliveryOrder): DeliveryOrderWithDetails {
        // 1. Fetch Customer
        val customer = order.customer_id?.let { custId ->
            database.customerDao().getCustomerById(custId)?.toDomainModel()
        }

        // 2. Fetch Items
        val items = database.deliveryDao().getItemsForDeliveryOrder(order.id).map { it.toDomainModel() }

        // 3. Fetch Products for Items
        val itemsWithProducts = items.map { item ->
            val product = database.productDao().getProductById(item.product_id)?.toDomainModel()
            DeliveryItemDetail(item, product)
        }

        return DeliveryOrderWithDetails(order, customer, itemsWithProducts)
    }
}
