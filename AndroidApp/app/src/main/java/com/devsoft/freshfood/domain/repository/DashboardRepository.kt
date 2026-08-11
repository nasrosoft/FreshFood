package com.devsoft.freshfood.domain.repository

interface DashboardRepository {
    suspend fun getTodaySalesTotal(): Double
    suspend fun getTodayProfitTotal(): Double
    suspend fun getTotalCustomerCredit(): Double
    suspend fun getLowStockCount(): Int
}
