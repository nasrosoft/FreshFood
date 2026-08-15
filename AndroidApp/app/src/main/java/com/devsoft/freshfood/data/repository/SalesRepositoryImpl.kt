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

            // 4. Atomic Transaction
            database.withTransaction {
                database.saleDao().insertSale(saleEntity)
                database.saleItemDao().insertSaleItems(saleItems)
                database.stockDao().insertStockMovements(stockMovements)
                
                paymentEntity?.let { database.paymentDao().insertPayment(it) }
                creditTxEntity?.let { database.creditTransactionDao().insertCreditTransaction(it) }
                
                // Enqueue the SaleRequest RPC payload
                val syncQueueEntity = SyncQueueEntity(
                    entity_type = "rpc_process_sale",
                    entity_id = saleId,
                    operation = "RPC",
                    payload = Json.encodeToString(saleRequest),
                    device_id = deviceId
                )
                database.syncQueueDao().insert(syncQueueEntity)
                
                // Update local customer credit locally if needed (omitted for brevity, assume UI calculates from transactions)
            }
            
            Result.success("INV-${saleId.take(8).uppercase()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
