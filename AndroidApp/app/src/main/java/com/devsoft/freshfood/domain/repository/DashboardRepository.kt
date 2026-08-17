package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.Sale

interface DashboardRepository {
    suspend fun getSales(): List<Sale>
    suspend fun getCustomers(): List<Customer>
    suspend fun getProducts(): List<Product>
    
    suspend fun getTodaySalesTotal(): Double
    suspend fun getTodayProfitTotal(): Double
    suspend fun getTotalCustomerCredit(): Double
    suspend fun getLowStockCount(): Int
}
