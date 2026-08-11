package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.SaleRequest
import com.devsoft.freshfood.domain.repository.SalesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class SalesRepositoryImpl(
    private val supabase: SupabaseClient
) : SalesRepository {

    override suspend fun processSale(saleRequest: SaleRequest): Result<String> {
        return try {
            // Invoking the PostgreSQL RPC function 'process_sale'
            // The argument must match the parameter name defined in the SQL function: 'sale_data'
            val response = supabase.postgrest.rpc(
                function = "process_sale",
                parameters = mapOf("sale_data" to saleRequest)
            )
            
            // Expected JSON response: {"success": true, "sale_id": "...", "invoice_number": "..."}
            val jsonResponse = response.decodeAs<JsonObject>()
            val invoiceNumber = jsonResponse["invoice_number"]?.jsonPrimitive?.content ?: "Unknown"
            Result.success(invoiceNumber)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
