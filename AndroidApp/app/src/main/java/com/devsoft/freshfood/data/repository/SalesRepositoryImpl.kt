package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.SaleRequest
import com.devsoft.freshfood.domain.repository.SalesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable

@Serializable
data class ProcessSaleParams(val sale_data: SaleRequest)

class SalesRepositoryImpl(
    private val supabase: SupabaseClient
) : SalesRepository {

    override suspend fun processSale(saleRequest: SaleRequest): Result<String> {
        return try {
            supabase.postgrest.rpc("process_sale", ProcessSaleParams(saleRequest))
            Result.success("INV-${saleRequest.id.take(8).uppercase()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
