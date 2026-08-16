package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.Sale
import com.devsoft.freshfood.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class DashboardRepositoryImpl(
    private val supabase: SupabaseClient
) : DashboardRepository {

    override suspend fun getTodaySalesTotal(): Double {
        return try {
            val sales = supabase.postgrest["sales"].select().decodeList<Sale>()
            sales.sumOf { it.total_amount }
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun getTodayProfitTotal(): Double {
        return getTodaySalesTotal() * 0.20
    }

    override suspend fun getTotalCustomerCredit(): Double {
        return try {
            val customers = supabase.postgrest["customers"].select().decodeList<Customer>()
            customers.sumOf { it.current_credit }
        } catch (e: Exception) {
            0.0
        }
    }

    override suspend fun getLowStockCount(): Int {
        return try {
            val products = supabase.postgrest["products"].select().decodeList<Product>()
            products.count { it.current_stock <= it.min_stock }
        } catch (e: Exception) {
            0
        }
    }
}
