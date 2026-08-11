package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun getCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: String): Customer?
    suspend fun registerPayment(payment: Payment)
}
