package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.SaleRequest
import com.devsoft.freshfood.domain.repository.SalesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class SalesRepositoryImpl(
    private val supabase: SupabaseClient
) : SalesRepository {

    override suspend fun processSale(saleRequest: SaleRequest): Result<String> {
        return try {
            supabase.postgrest.rpc("process_sale", saleRequest)
            Result.success("INV-${saleRequest.id.take(8).uppercase()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
