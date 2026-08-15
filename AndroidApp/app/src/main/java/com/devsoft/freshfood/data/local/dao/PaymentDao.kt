package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devsoft.freshfood.data.local.entity.PaymentEntity
import com.devsoft.freshfood.data.local.entity.CreditTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Query("SELECT * FROM payments WHERE customer_id = :customerId AND deleted_at IS NULL")
    fun getPaymentsForCustomer(customerId: String): Flow<List<PaymentEntity>>
}

@Dao
interface CreditTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditTransaction(creditTransaction: CreditTransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditTransactions(creditTransactions: List<CreditTransactionEntity>)

    @Query("SELECT * FROM credit_transactions WHERE customer_id = :customerId AND deleted_at IS NULL")
    fun getTransactionsForCustomer(customerId: String): Flow<List<CreditTransactionEntity>>
}
