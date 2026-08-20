package com.devsoft.devsoft.data.repository

import com.devsoft.devsoft.domain.model.Customer
import com.devsoft.devsoft.domain.model.CustomerCreditDetail
import com.devsoft.devsoft.domain.model.Payment
import com.devsoft.devsoft.domain.repository.CustomerRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
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

    override suspend fun getCustomerCreditDetails(customerId: String): List<CustomerCreditDetail> {
        return try {
            supabase.postgrest["customer_credit_details"]
                .select {
                    filter { eq("customer_id", customerId) }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<CustomerCreditDetail>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun registerPayment(payment: Payment) {
        // 1. Record payment entry
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

        // 2. Record credit transaction
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

        // 3. Record in customer_credit_details for audit trace
        try {
            val detailMap = mapOf(
                "customer_id" to payment.customer_id,
                "items_summary" to "Paiement de crédit / Credit Payment",
                "total_amount" to payment.amount,
                "credit_amount" to payment.amount,
                "transaction_type" to "PAYMENT"
            )
            supabase.postgrest["customer_credit_details"].insert(detailMap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
