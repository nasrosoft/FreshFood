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

    override suspend fun getSales(): List<Sale> {
        return try {
            supabase.postgrest["sales"].select().decodeList<Sale>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getCustomers(): List<Customer> {
        return try {
            supabase.postgrest["customers"].select().decodeList<Customer>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getProducts(): List<Product> {
        return try {
            supabase.postgrest["products"].select().decodeList<Product>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getDeliveryOrders(): List<com.devsoft.freshfood.domain.model.DeliveryOrder> {
        return try {
            supabase.postgrest["delivery_orders"].select().decodeList<com.devsoft.freshfood.domain.model.DeliveryOrder>()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getTodaySalesTotal(): Double {
        return getSales().sumOf { it.total_amount }
    }

    override suspend fun getTodayProfitTotal(): Double {
        return getTodaySalesTotal() * 0.20
    }

    override suspend fun getTotalCustomerCredit(): Double {
        return getCustomers().sumOf { it.current_credit }
    }

    override suspend fun getLowStockCount(): Int {
        return getProducts().count { it.current_stock <= it.min_stock }
    }
}
