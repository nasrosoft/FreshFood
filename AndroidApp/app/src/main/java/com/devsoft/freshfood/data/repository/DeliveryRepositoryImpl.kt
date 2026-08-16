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
import java.util.UUID
import java.time.Instant
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.devsoft.freshfood.data.local.entity.StockMovementEntity
import com.devsoft.freshfood.data.sync.SyncWorker

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
                // Stock reduction logic
                if (newStatus == "DELIVERED" && order.status != "DELIVERED") {
                    val items = database.deliveryDao().getItemsForDeliveryOrder(id)
                    val stockMovements = mutableListOf<StockMovementEntity>()
                    val now = Instant.now().toString()
                    
                    items.forEach { item ->
                        stockMovements.add(
                            StockMovementEntity(
                                id = UUID.randomUUID().toString(),
                                product_id = item.product_id,
                                batch_id = null,
                                movement_type = "DELIVERY",
                                quantity = -item.quantity,
                                reference_id = id,
                                user_id = order.delivery_employee_id ?: "",
                                created_at = now
                            )
                        )
                        val product = database.productDao().getProductById(item.product_id)
                        if (product != null) {
                            database.productDao().updateProduct(product.copy(current_stock = product.current_stock - item.quantity))
                        }
                    }
                    database.stockDao().insertStockMovements(stockMovements)
                } else if (order.status == "DELIVERED" && newStatus != "DELIVERED") {
                    // Restock if status changed from DELIVERED to something else (e.g. RETURNED)
                    val items = database.deliveryDao().getItemsForDeliveryOrder(id)
                    val stockMovements = mutableListOf<StockMovementEntity>()
                    val now = Instant.now().toString()
                    
                    items.forEach { item ->
                        stockMovements.add(
                            StockMovementEntity(
                                id = UUID.randomUUID().toString(),
                                product_id = item.product_id,
                                batch_id = null,
                                movement_type = "DELIVERY_RETURN",
                                quantity = item.quantity,
                                reference_id = id,
                                user_id = order.delivery_employee_id ?: "",
                                created_at = now
                            )
                        )
                        val product = database.productDao().getProductById(item.product_id)
                        if (product != null) {
                            database.productDao().updateProduct(product.copy(current_stock = product.current_stock + item.quantity))
                        }
                    }
                    database.stockDao().insertStockMovements(stockMovements)
                }
            }
        }
        
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<SyncWorker>().build())
    }

    override suspend fun deleteDeliveryOrder(id: String) {
        val deviceId = DeviceUtil.getDeviceId(context)
        
        database.withTransaction {
            val order = database.deliveryDao().getDeliveryOrderById(id)
            if (order != null) {
                database.deliveryDao().deleteItemsForDeliveryOrder(id)
                database.deliveryDao().deleteDeliveryOrderById(id)
                
                database.syncQueueDao().insert(
                    SyncQueueEntity(
                        entity_type = "delivery_orders",
                        entity_id = id,
                        operation = "DELETE",
                        payload = Json.encodeToString(order.toDomainModel()),
                        device_id = deviceId
                    )
                )
            }
        }
        
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<SyncWorker>().build())
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
