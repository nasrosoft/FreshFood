package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class DashboardRepositoryImpl(
    private val supabase: SupabaseClient
) : DashboardRepository {

    override suspend fun getTodaySalesTotal(): Double {
        // In a real implementation, you would filter sales by today's date.
        // For scaffolding, we fetch all sales and sum.
        val sales = supabase.postgrest["sales"].select().decodeList<JsonObject>()
        return sales.sumOf { it["total_amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0 }
    }

    override suspend fun getTodayProfitTotal(): Double {
        // Profit is calculated by SaleItem (selling_price - cost_price).
        // Scaffolding default to 20% of sales
        return getTodaySalesTotal() * 0.20
    }

    override suspend fun getTotalCustomerCredit(): Double {
        val customers = supabase.postgrest["customers"].select().decodeList<JsonObject>()
        return customers.sumOf { it["current_credit"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0 }
    }

    override suspend fun getLowStockCount(): Int {
        val products = supabase.postgrest["products"].select().decodeList<JsonObject>()
        return products.count { 
            val current = it["current_stock"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val min = it["min_stock"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            current <= min
        }
    }
}
