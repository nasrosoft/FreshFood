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
        val customers = supabase.postgrest["customers"].select().decodeList<Customer>()
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
        val customer = getCustomerById(payment.customer_id)
        if (customer != null) {
            val newCredit = (customer.current_credit - payment.amount).coerceAtLeast(0.0)
            supabase.postgrest["payments"].insert(payment)
            
            val creditTx = mapOf(
                "customer_id" to payment.customer_id,
                "amount" to payment.amount,
                "transaction_type" to "PAYMENT",
                "reference_id" to payment.id,
                "user_id" to payment.user_id
            )
            supabase.postgrest["credit_transactions"].insert(creditTx)
            
            supabase.postgrest["customers"].update(
                {
                    set("current_credit", newCredit)
                }
            ) {
                filter { eq("id", customer.id) }
            }
        }
    }
}
