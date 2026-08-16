package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.PurchaseRequest
import com.devsoft.freshfood.domain.repository.PurchaseRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class PurchaseRepositoryImpl(
    private val supabase: SupabaseClient
) : PurchaseRepository {
    override suspend fun processPurchase(request: PurchaseRequest): Result<String> {
        return try {
            supabase.postgrest.rpc("process_purchase", request)
            Result.success("PUR-${request.id.take(8).uppercase()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
