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
            
            // 1. Immediately update customer's current credit in Supabase
            supabase.postgrest["customers"].update(
                {
                    set("current_credit", newCredit)
                }
            ) {
                filter { eq("id", customer.id) }
            }

            // 2. Record payment entry safely
            try {
                val paymentMap = mutableMapOf<String, Any>(
                    "customer_id" to payment.customer_id,
                    "amount" to payment.amount,
                    "payment_method" to payment.payment_method
                )
                if (!payment.user_id.isNullOrBlank() && payment.user_id != "00000000-0000-0000-0000-000000000000") {
                    paymentMap["user_id"] = payment.user_id
                }
                supabase.postgrest["payments"].insert(paymentMap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // 3. Record credit transaction safely
            try {
                val creditTx = mutableMapOf<String, Any>(
                    "customer_id" to payment.customer_id,
                    "amount" to payment.amount,
                    "transaction_type" to "PAYMENT"
                )
                if (!payment.user_id.isNullOrBlank() && payment.user_id != "00000000-0000-0000-0000-000000000000") {
                    creditTx["user_id"] = payment.user_id
                }
                supabase.postgrest["credit_transactions"].insert(creditTx)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
