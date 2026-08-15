package com.devsoft.freshfood.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.devsoft.freshfood.data.local.FreshFoodDatabase
import com.devsoft.freshfood.data.local.dao.CustomerDao
import com.devsoft.freshfood.data.local.dao.PaymentDao
import com.devsoft.freshfood.data.local.dao.CreditTransactionDao
import com.devsoft.freshfood.data.local.dao.SyncQueueDao
import com.devsoft.freshfood.data.local.entity.CustomerEntity
import com.devsoft.freshfood.data.local.entity.PaymentEntity
import com.devsoft.freshfood.data.local.entity.CreditTransactionEntity
import com.devsoft.freshfood.data.local.entity.SyncQueueEntity
import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.model.Payment
import com.devsoft.freshfood.domain.repository.CustomerRepository
import com.devsoft.freshfood.utils.DeviceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class CustomerRepositoryImpl(
    private val database: FreshFoodDatabase,
    private val context: Context
) : CustomerRepository {
    private val customerDao = database.customerDao()
    private val paymentDao = database.paymentDao()
    private val creditTransactionDao = database.creditTransactionDao()
    private val syncQueueDao = database.syncQueueDao()

    override suspend fun getCustomers(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities -> 
            entities.map { it.toDomainModel() } 
        }
    }

    override suspend fun getCustomerById(id: String): Customer? {
        return customerDao.getCustomerById(id)?.toDomainModel()
    }

    override suspend fun insertCustomer(customer: Customer) {
        val entity = CustomerEntity.fromDomainModel(customer)
        
        database.withTransaction {
            customerDao.insertCustomer(entity)
            syncQueueDao.insert(
                SyncQueueEntity(
                    entity_type = "customers",
                    entity_id = customer.id,
                    operation = "CREATE",
                    payload = Json.encodeToString(customer),
                    device_id = DeviceUtil.getDeviceId(context)
                )
            )
        }
    }

    override suspend fun registerPayment(payment: Payment) {
        val paymentEntity = PaymentEntity.fromDomainModel(payment)
        val creditTxId = UUID.randomUUID().toString()
        val creditTxEntity = CreditTransactionEntity(
            id = creditTxId,
            customer_id = payment.customer_id,
            amount = payment.amount,
            transaction_type = "PAYMENT",
            reference_id = payment.id,
            user_id = payment.user_id,
            created_at = null
        )

        database.withTransaction {
            // Update customer credit locally
            val customer = customerDao.getCustomerById(payment.customer_id)
            if (customer != null) {
                val newCredit = (customer.current_credit - payment.amount).coerceAtLeast(0.0)
                customerDao.updateCustomer(customer.copy(current_credit = newCredit))
                
                // Add customer update to sync queue
                syncQueueDao.insert(
                    SyncQueueEntity(
                        entity_type = "customers",
                        entity_id = customer.id,
                        operation = "UPDATE",
                        payload = Json.encodeToString(customer.toDomainModel().copy(current_credit = newCredit)),
                        device_id = DeviceUtil.getDeviceId(context)
                    )
                )
            }

            paymentDao.insertPayment(paymentEntity)
            creditTransactionDao.insertCreditTransaction(creditTxEntity)
            
            // Queue payment sync
            syncQueueDao.insert(
                SyncQueueEntity(
                    entity_type = "payments",
                    entity_id = paymentEntity.id,
                    operation = "CREATE",
                    payload = Json.encodeToString(payment),
                    device_id = DeviceUtil.getDeviceId(context)
                )
            )
            // No need to queue credit_transactions if the backend trigger handles it on payment insert, 
            // but if we are moving to raw inserts, we should queue both or call an RPC.
            // For now, we'll let SyncManager push the payment, and we can queue the creditTx as well.
            syncQueueDao.insert(
                SyncQueueEntity(
                    entity_type = "credit_transactions",
                    entity_id = creditTxId,
                    operation = "CREATE",
                    payload = Json.encodeToString(mapOf(
                        "customer_id" to payment.customer_id,
                        "amount" to payment.amount,
                        "transaction_type" to "PAYMENT",
                        "user_id" to payment.user_id
                    )),
                    device_id = DeviceUtil.getDeviceId(context)
                )
            )
        }
    }
}
