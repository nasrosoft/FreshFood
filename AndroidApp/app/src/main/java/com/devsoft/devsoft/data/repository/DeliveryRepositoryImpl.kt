package com.devsoft.devsoft.data.repository

import com.devsoft.devsoft.domain.model.*
import com.devsoft.devsoft.domain.repository.DeliveryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.Serializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Serializable
data class SyncDeliveryParams(
    val p_delivery_id: String,
    val p_modified_quantities: Map<String, Int>,
    val p_final_payment_method: String
)

class DeliveryRepositoryImpl(
    private val supabase: SupabaseClient
) : DeliveryRepository {

    override suspend fun getDeliveries(): Flow<List<DeliveryOrderWithDetails>> = flow {
        val orders = supabase.postgrest["delivery_orders"].select().decodeList<DeliveryOrder>()
        val customers = supabase.postgrest["customers"].select().decodeList<Customer>()
        val allItems = supabase.postgrest["delivery_items"].select().decodeList<DeliveryItem>()
        val products = supabase.postgrest["products"].select().decodeList<Product>()
        val profiles = try {
            supabase.postgrest["profiles"].select().decodeList<Profile>()
        } catch (e: Exception) {
            emptyList()
        }
        val saleIds = orders.mapNotNull { it.sale_id }.distinct()
        val sales = if (saleIds.isNotEmpty()) {
            supabase.postgrest["sales"].select { filter { isIn("id", saleIds) } }.decodeList<Sale>()
        } else {
            emptyList()
        }

        val detailedOrders = orders.map { order ->
            val customer = customers.find { it.id == order.customer_id }
            val driver = profiles.find { it.id == order.delivery_employee_id }
            val orderItems = allItems.filter { it.delivery_order_id == order.id }.map { item ->
                val product = products.find { it.id == item.product_id }
                DeliveryItemDetail(item, product)
            }
            val sale = sales.find { it.id == order.sale_id }
            DeliveryOrderWithDetails(order, customer, driver, orderItems, sale)
        }
        
        emit(detailedOrders)
    }

    override suspend fun getDeliveryById(id: String): DeliveryOrderWithDetails? {
        val order = supabase.postgrest["delivery_orders"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<DeliveryOrder>() ?: return null
            
        val customer = order.customer_id?.let {
            supabase.postgrest["customers"].select { filter { eq("id", it) } }.decodeSingleOrNull<Customer>()
        }
        
        val driver = order.delivery_employee_id?.let {
            try {
                supabase.postgrest["profiles"].select { filter { eq("id", it) } }.decodeSingleOrNull<Profile>()
            } catch (e: Exception) {
                null
            }
        }
        
        val sale = order.sale_id?.let {
            supabase.postgrest["sales"].select { filter { eq("id", it) } }.decodeSingleOrNull<Sale>()
        }
        
        val items = supabase.postgrest["delivery_items"]
            .select { filter { eq("delivery_order_id", id) } }
            .decodeList<DeliveryItem>()
            
        val productIds = items.map { it.product_id }.distinct()
        
        val products = if (productIds.isNotEmpty()) {
            supabase.postgrest["products"]
                .select { filter { isIn("id", productIds) } }
                .decodeList<Product>()
        } else {
            emptyList()
        }
        
        val orderItems = items.map { item ->
            DeliveryItemDetail(item, products.find { it.id == item.product_id })
        }
        
        return DeliveryOrderWithDetails(order, customer, driver, orderItems, sale)
    }

    override suspend fun updateDeliveryStatus(id: String, newStatus: String) {
        val now = java.time.Instant.now().toString()
        supabase.postgrest["delivery_orders"].update(
            {
                set("status", newStatus)
                set("updated_at", now)
            }
        ) {
            filter { eq("id", id) }
        }
    }

    override suspend fun updateDeliveryItemsAndComplete(orderId: String, modifiedQuantities: Map<String, Int>, finalPaymentMethod: String?) {
        val params = SyncDeliveryParams(
            p_delivery_id = orderId,
            p_modified_quantities = modifiedQuantities,
            p_final_payment_method = finalPaymentMethod ?: "CASH"
        )
        supabase.postgrest.rpc("sync_delivery_completion", params)
    }

    override suspend fun deleteDeliveryOrder(id: String) {
        supabase.postgrest["delivery_orders"].delete { filter { eq("id", id) } }
    }
}
