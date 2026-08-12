package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.model.Payment
import com.devsoft.freshfood.domain.repository.CustomerRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CustomerRepositoryImpl(
    private val supabase: SupabaseClient
) : CustomerRepository {

    override suspend fun getCustomers(): Flow<List<Customer>> = flow {
        val customers = supabase.postgrest["customers"]
            .select()
            .decodeList<Customer>()
        emit(customers)
    }

    override suspend fun getCustomerById(id: String): Customer? {
        return supabase.postgrest["customers"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<Customer>()
    }

    override suspend fun insertCustomer(customer: Customer) {
        supabase.postgrest["customers"].insert(customer)
    }

    override suspend fun registerPayment(payment: Payment) {
        // We use an RPC function or a single transaction. 
        // Here we insert the payment directly. The trigger `trigger_update_customer_credit` 
        // on `credit_transactions` will handle updating `current_credit`.
        // Wait, to trigger that we need to insert a credit_transaction.
        // For now, we insert payment and credit_transaction manually or let a Postgres function handle it.
        // Inserting Payment:
        supabase.postgrest["payments"].insert(payment)
        
        // Inserting Credit Transaction (PAYMENT)
        val creditTx = mapOf(
            "customer_id" to payment.customer_id,
            "amount" to payment.amount,
            "transaction_type" to "PAYMENT",
            "user_id" to payment.user_id
        )
        supabase.postgrest["credit_transactions"].insert(creditTx)
    }
}
