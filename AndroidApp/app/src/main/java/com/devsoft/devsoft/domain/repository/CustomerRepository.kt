package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.Customer
import com.devsoft.devsoft.domain.model.CustomerCreditDetail
import com.devsoft.devsoft.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun getCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: String): Customer?
    suspend fun insertCustomer(customer: Customer)
    suspend fun deleteCustomer(id: String)
    suspend fun registerPayment(payment: Payment)
    suspend fun getCustomerCreditDetails(customerId: String): List<CustomerCreditDetail>
}
