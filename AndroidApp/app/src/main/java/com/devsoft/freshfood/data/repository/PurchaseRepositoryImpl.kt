package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.PurchaseRequest
import com.devsoft.freshfood.domain.repository.PurchaseRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

class PurchaseRepositoryImpl(
    private val supabase: SupabaseClient
) : PurchaseRepository {
    override suspend fun processPurchase(request: PurchaseRequest): Result<String> {
        return try {
            val response = supabase.postgrest.rpc(
                function = "process_purchase",
                parameters = request
            ).decodeAs<JsonObject>()
            
            val success = response["success"]?.jsonPrimitive?.booleanOrNull ?: false
            if (success) {
                Result.success(response["purchase_id"]?.jsonPrimitive?.content ?: "Unknown ID")
            } else {
                Result.failure(Exception("Purchase failed according to backend"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
