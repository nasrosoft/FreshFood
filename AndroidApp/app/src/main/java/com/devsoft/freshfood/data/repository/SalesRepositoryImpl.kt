package com.devsoft.freshfood.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.devsoft.freshfood.data.local.FreshFoodDatabase
import com.devsoft.freshfood.data.local.entity.CreditTransactionEntity
import com.devsoft.freshfood.data.local.entity.PaymentEntity
import com.devsoft.freshfood.data.local.entity.SaleEntity
import com.devsoft.freshfood.data.local.entity.SaleItemEntity
import com.devsoft.freshfood.data.local.entity.StockMovementEntity
import com.devsoft.freshfood.data.local.entity.SyncQueueEntity
import com.devsoft.freshfood.data.local.entity.DeliveryOrderEntity
import com.devsoft.freshfood.data.local.entity.DeliveryItemEntity
import com.devsoft.freshfood.data.local.entity.NotificationEntity
import com.devsoft.freshfood.domain.model.SaleRequest
import com.devsoft.freshfood.domain.repository.SalesRepository
import com.devsoft.freshfood.utils.DeviceUtil
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class SalesRepositoryImpl(
    private val database: FreshFoodDatabase,
    private val context: Context
) : SalesRepository {

    override suspend fun processSale(saleRequest: SaleRequest): Result<String> {
        return try {
            val saleId = saleRequest.id
            val paymentId = saleRequest.payment_id ?: UUID.randomUUID().toString()
            val creditTxId = saleRequest.credit_transaction_id ?: UUID.randomUUID().toString()
            val createdAt = saleRequest.created_at ?: java.time.Instant.now().toString()
            
            // 1. Create Sale Entity
            val saleEntity = SaleEntity(
                id = saleId,
                invoice_number = "INV-${saleId.take(8).uppercase()}",
                customer_id = saleRequest.customer_id,
                user_id = saleRequest.user_id,
                total_amount = saleRequest.total_amount,
                paid_amount = saleRequest.paid_amount,
                credit_amount = saleRequest.credit_amount,
                payment_method = saleRequest.payment_method,
                status = "COMPLETED"
            )
            
            // 2. Create Sale Items & Stock Movements
            val saleItems = mutableListOf<SaleItemEntity>()
            val stockMovements = mutableListOf<StockMovementEntity>()
            
            saleRequest.items.forEach { item ->
                saleItems.add(
                    SaleItemEntity(
                        id = item.id,
                        sale_id = saleId,
                        product_id = item.product_id,
                        quantity = item.quantity,
                        unit_price = item.unit_price,
                        subtotal = item.quantity * item.unit_price,
                        created_at = createdAt
                    )
                )
                
                stockMovements.add(
                    StockMovementEntity(
                        id = UUID.randomUUID().toString(),
                        product_id = item.product_id,
                        batch_id = null, // Backend process_sale RPC handles specific batch deduction
                        movement_type = "SALE",
                        quantity = -item.quantity, // Negative for OUT
                        reference_id = saleId,
                        user_id = saleRequest.user_id ?: "",
                        created_at = createdAt
                    )
                )
            }
            
            // 3. Optional Payment & Credit Entities
            var paymentEntity: PaymentEntity? = null
            var creditTxEntity: CreditTransactionEntity? = null
            
            if (saleRequest.paid_amount > 0 && saleRequest.customer_id != null) {
                paymentEntity = PaymentEntity(
                    id = paymentId,
                    customer_id = saleRequest.customer_id,
                    amount = saleRequest.paid_amount,
                    payment_method = saleRequest.payment_method,
                    reference_id = saleId,
                    user_id = saleRequest.user_id ?: ""
                )
                
                creditTxEntity = CreditTransactionEntity(
                    id = creditTxId,
                    customer_id = saleRequest.customer_id,
                    amount = saleRequest.paid_amount,
                    transaction_type = "PAYMENT",
                    reference_id = paymentId,
                    user_id = saleRequest.user_id ?: "",
                    created_at = createdAt
                )
            }
            
            val deviceId = DeviceUtil.getDeviceId(context)

            var deliveryOrderEntity: DeliveryOrderEntity? = null
            val deliveryItems = mutableListOf<DeliveryItemEntity>()
            var notificationEntity: NotificationEntity? = null
            
            if (saleRequest.create_delivery) {
                val deliveryOrderId = UUID.randomUUID().toString()
                deliveryOrderEntity = DeliveryOrderEntity(
                    id = deliveryOrderId,
                    customer_id = saleRequest.customer_id,
                    delivery_employee_id = saleRequest.delivery_employee_id,
                    status = "PENDING",
                    notes = "Created from POS Sale: INV-${saleId.take(8).uppercase()}",
                    created_at = createdAt
                )
                
                saleRequest.items.forEach { item ->
                    deliveryItems.add(
                        DeliveryItemEntity(
                            id = UUID.randomUUID().toString(),
                            delivery_order_id = deliveryOrderId,
                            product_id = item.product_id,
                            quantity = item.quantity,
                            created_at = createdAt
                        )
                    )
                }
                
                if (saleRequest.delivery_employee_id != null) {
                    notificationEntity = NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        user_id = saleRequest.delivery_employee_id,
                        title = "New Delivery Assigned",
                        message = "You have been assigned a new delivery for Invoice INV-${saleId.take(8).uppercase()}.",
                        is_read = false,
                        created_at = createdAt
                    )
                }
            }

            // 4. Atomic Transaction
            database.withTransaction {
                database.saleDao().insertSale(saleEntity)
                database.saleItemDao().insertSaleItems(saleItems)
                database.stockDao().insertStockMovements(stockMovements)
                
                paymentEntity?.let { database.paymentDao().insertPayment(it) }
                creditTxEntity?.let { database.creditTransactionDao().insertCreditTransaction(it) }
                
                val syncQueueEntity = SyncQueueEntity(
                    entity_type = "rpc_process_sale",
                    entity_id = saleId,
                    operation = "RPC",
                    payload = Json.encodeToString(saleRequest),
                    device_id = deviceId
                )
                database.syncQueueDao().insert(syncQueueEntity)
                
                deliveryOrderEntity?.let {
                    database.deliveryDao().insertDeliveryOrder(it)
                    database.deliveryDao().insertDeliveryItems(deliveryItems)
                    
                    val deliveryQueueOp = SyncQueueEntity(
                        entity_type = "delivery_orders",
                        entity_id = it.id,
                        operation = "CREATE",
                        payload = Json.encodeToString(com.devsoft.freshfood.domain.model.DeliveryOrder(
                            id = it.id,
                            customer_id = it.customer_id,
                            delivery_employee_id = it.delivery_employee_id,
                            status = it.status,
                            notes = it.notes,
                            created_at = it.created_at
                        )),
                        device_id = deviceId
                    )
                    database.syncQueueDao().insert(deliveryQueueOp)
                    
                    deliveryItems.forEach { item ->
                        val itemOp = SyncQueueEntity(
                            entity_type = "delivery_items",
                            entity_id = item.id,
                            operation = "CREATE",
                            payload = Json.encodeToString(com.devsoft.freshfood.domain.model.DeliveryItem(
                                id = item.id,
                                delivery_order_id = item.delivery_order_id,
                                product_id = item.product_id,
                                quantity = item.quantity,
                                created_at = item.created_at
                            )),
                            device_id = deviceId
                        )
                        database.syncQueueDao().insert(itemOp)
                    }
                }
                
                notificationEntity?.let {
                    database.notificationDao().insertNotifications(listOf(it))
                    val notifOp = SyncQueueEntity(
                        entity_type = "notifications",
                        entity_id = it.id,
                        operation = "CREATE",
                        payload = Json.encodeToString(it),
                        device_id = deviceId
                    )
                    database.syncQueueDao().insert(notifOp)
                }
                
                // Update local customer credit locally if needed (omitted for brevity, assume UI calculates from transactions)
            }
            
            Result.success("INV-${saleId.take(8).uppercase()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
