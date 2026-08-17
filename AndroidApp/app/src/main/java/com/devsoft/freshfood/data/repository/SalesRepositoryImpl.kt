package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.Customer
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

            // If this is a credit sale with a selected customer, ensure customer's debt is updated in Supabase
            if (saleRequest.credit_amount > 0 && !saleRequest.customer_id.isNullOrBlank()) {
                try {
                    val customer = supabase.postgrest["customers"]
                        .select { filter { eq("id", saleRequest.customer_id) } }
                        .decodeSingleOrNull<Customer>()
                    if (customer != null) {
                        val newCredit = customer.current_credit + saleRequest.credit_amount
                        supabase.postgrest["customers"].update(
                            { set("current_credit", newCredit) }
                        ) {
                            filter { eq("id", customer.id) }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            Result.success("INV-${saleRequest.id.take(8).uppercase()}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
