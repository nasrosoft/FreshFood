package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.Customer
import com.devsoft.devsoft.domain.model.Product
import com.devsoft.devsoft.domain.model.Sale

interface DashboardRepository {
    suspend fun getSales(): List<Sale>
    suspend fun getCustomers(): List<Customer>
    suspend fun getProducts(): List<Product>
    suspend fun getDeliveryOrders(): List<com.devsoft.devsoft.domain.model.DeliveryOrder>
    
    suspend fun getTodaySalesTotal(): Double
    suspend fun getTodayProfitTotal(): Double
    suspend fun getTotalCustomerCredit(): Double
    suspend fun getLowStockCount(): Int
}
