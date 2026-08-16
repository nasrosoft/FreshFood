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
                // Stock reduction logic is now handled at the time of sale.
                // Only restock if delivery is cancelled.
                if (newStatus == "CANCELLED" && order.status != "CANCELLED") {
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

    override suspend fun updateDeliveryItemsAndComplete(orderId: String, modifiedQuantities: Map<String, Int>) {
        val deviceId = DeviceUtil.getDeviceId(context)
        val now = Instant.now().toString()

        database.withTransaction {
            val order = database.deliveryDao().getDeliveryOrderById(orderId) ?: return@withTransaction
            val items = database.deliveryDao().getItemsForDeliveryOrder(orderId)
            var totalRefundAmount = 0.0

            items.forEach { item ->
                val newQty = modifiedQuantities[item.product_id] ?: item.quantity
                if (newQty < item.quantity) {
                    val diff = item.quantity - newQty
                    
                    // 1. Restock physical item locally
                    database.stockDao().insertStockMovements(listOf(
                        StockMovementEntity(
                            id = UUID.randomUUID().toString(),
                            product_id = item.product_id,
                            batch_id = null,
                            movement_type = "DELIVERY_RETURN",
                            quantity = diff,
                            reference_id = orderId,
                            user_id = order.delivery_employee_id ?: "",
                            created_at = now
                        )
                    ))
                    
                    val product = database.productDao().getProductById(item.product_id)
                    if (product != null) {
                        database.productDao().updateProduct(product.copy(current_stock = product.current_stock + diff))
                    }

                    // 2. Update local DeliveryItem and Queue Sync
                    if (newQty == 0) {
                        database.deliveryDao().deleteDeliveryItemById(item.id)
                        database.syncQueueDao().insert(
                            SyncQueueEntity(
                                entity_type = "delivery_items",
                                entity_id = item.id,
                                operation = "DELETE",
                                payload = Json.encodeToString(item.toDomainModel()),
                                device_id = deviceId
                            )
                        )
                    } else {
                        val updatedItem = item.copy(quantity = newQty)
                        database.deliveryDao().updateDeliveryItem(updatedItem)
                        database.syncQueueDao().insert(
                            SyncQueueEntity(
                                entity_type = "delivery_items",
                                entity_id = item.id,
                                operation = "UPDATE",
                                payload = Json.encodeToString(mapOf("id" to item.id, "quantity" to newQty.toString())),
                                device_id = deviceId
                            )
                        )
                    }

                    // 3. Handle financial records locally if linked to a sale
                    if (order.sale_id != null) {
                        val saleItems = database.saleItemDao().getItemsForSale(order.sale_id)
                        val matchingSaleItem = saleItems.find { it.product_id == item.product_id }
                        if (matchingSaleItem != null) {
                            val refundForThisItem = diff * matchingSaleItem.unit_price
                            totalRefundAmount += refundForThisItem
                            
                            val updatedSaleItem = matchingSaleItem.copy(
                                quantity = matchingSaleItem.quantity - diff,
                                subtotal = matchingSaleItem.subtotal - refundForThisItem
                            )
                            database.saleItemDao().updateSaleItem(updatedSaleItem)
                        }
                    }
                }
            }

            if (totalRefundAmount > 0 && order.sale_id != null) {
                val sale = database.saleDao().getSaleById(order.sale_id)
                if (sale != null) {
                    val updatedSale = sale.copy(
                        total_amount = sale.total_amount - totalRefundAmount
                    )
                    database.saleDao().updateSale(updatedSale)
                    
                    if (order.customer_id != null) {
                        database.creditTransactionDao().insertCreditTransaction(
                            com.devsoft.freshfood.data.local.entity.CreditTransactionEntity(
                                id = UUID.randomUUID().toString(),
                                customer_id = order.customer_id,
                                amount = totalRefundAmount,
                                transaction_type = "PAYMENT",
                                reference_id = order.sale_id,
                                user_id = order.delivery_employee_id ?: "",
                                created_at = now
                            )
                        )
                        val customer = database.customerDao().getCustomerById(order.customer_id)
                        if (customer != null) {
                            val newCredit = if (customer.current_credit - totalRefundAmount < 0) 0.0 else customer.current_credit - totalRefundAmount
                            database.customerDao().updateCustomer(
                                customer.copy(current_credit = newCredit)
                            )
                        }
                    }
                }
            }

            // Update status to DELIVERED
            database.deliveryDao().updateDeliveryOrder(order.copy(status = "DELIVERED"))
            database.syncQueueDao().insert(
                SyncQueueEntity(
                    entity_type = "delivery_orders",
                    entity_id = orderId,
                    operation = "UPDATE",
                    payload = Json.encodeToString(mapOf("id" to orderId, "status" to "DELIVERED")),
                    device_id = deviceId
                )
            )
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
