package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.data.local.FreshFoodDatabase
import com.devsoft.freshfood.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.first

class DashboardRepositoryImpl(
    private val database: FreshFoodDatabase
) : DashboardRepository {

    override suspend fun getTodaySalesTotal(): Double {
        val sales = database.saleDao().getAllSales().first() // Use first() to get current list from flow
        return sales.sumOf { it.total_amount }
    }

    override suspend fun getTodayProfitTotal(): Double {
        // Profit is calculated by SaleItem (selling_price - cost_price).
        // Scaffolding default to 20% of sales
        return getTodaySalesTotal() * 0.20
    }

    override suspend fun getTotalCustomerCredit(): Double {
        val customers = database.customerDao().getAllCustomers().first()
        return customers.sumOf { it.current_credit }
    }

    override suspend fun getLowStockCount(): Int {
        val products = database.productDao().getAllProducts().first()
        return products.count { it.current_stock <= it.min_stock }
    }
}
