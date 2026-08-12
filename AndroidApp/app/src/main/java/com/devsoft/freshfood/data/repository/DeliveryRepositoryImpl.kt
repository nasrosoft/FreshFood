package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.*
import com.devsoft.freshfood.domain.repository.DeliveryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeliveryRepositoryImpl(
    private val supabase: SupabaseClient
) : DeliveryRepository {

    override suspend fun getDeliveries(): Flow<List<DeliveryOrderWithDetails>> = flow {
        // Fetch all delivery orders
        val orders = supabase.postgrest["delivery_orders"]
            .select()
            .decodeList<DeliveryOrder>()

        val ordersWithDetails = orders.map { order ->
            enrichDeliveryOrder(order)
        }

        emit(ordersWithDetails)
    }

    override suspend fun getDeliveryById(id: String): DeliveryOrderWithDetails? {
        val order = supabase.postgrest["delivery_orders"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<DeliveryOrder>()

        return order?.let { enrichDeliveryOrder(it) }
    }

    override suspend fun updateDeliveryStatus(id: String, newStatus: String) {
        val updateData = mapOf("status" to newStatus)
        supabase.postgrest["delivery_orders"]
            .update(updateData) {
                filter { eq("id", id) }
            }
    }

    private suspend fun enrichDeliveryOrder(order: DeliveryOrder): DeliveryOrderWithDetails {
        // 1. Fetch Customer
        val customer = order.customer_id?.let { custId ->
            supabase.postgrest["customers"]
                .select { filter { eq("id", custId) } }
                .decodeSingleOrNull<Customer>()
        }

        // 2. Fetch Items
        val items = supabase.postgrest["delivery_items"]
            .select { filter { eq("delivery_order_id", order.id) } }
            .decodeList<DeliveryItem>()

        // 3. Fetch Products for Items
        val itemsWithProducts = items.map { item ->
            val product = supabase.postgrest["products"]
                .select { filter { eq("id", item.product_id) } }
                .decodeSingleOrNull<Product>()
            DeliveryItemDetail(item, product)
        }

        return DeliveryOrderWithDetails(order, customer, itemsWithProducts)
    }
}
