package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.DeliveryItem
import com.devsoft.freshfood.domain.model.DeliveryOrder

@Entity(tableName = "delivery_orders")
data class DeliveryOrderEntity(
    @PrimaryKey val id: String,
    val customer_id: String? = null,
    val delivery_employee_id: String? = null,
    val status: String = "PENDING",
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    fun toDomainModel(): DeliveryOrder {
        return DeliveryOrder(
            id = id,
            customer_id = customer_id,
            delivery_employee_id = delivery_employee_id,
            status = status,
            notes = notes,
            created_at = created_at,
            updated_at = updated_at
        )
    }

    companion object {
        fun fromDomainModel(order: DeliveryOrder): DeliveryOrderEntity {
            return DeliveryOrderEntity(
                id = order.id,
                customer_id = order.customer_id,
                delivery_employee_id = order.delivery_employee_id,
                status = order.status,
                notes = order.notes,
                created_at = order.created_at,
                updated_at = order.updated_at
            )
        }
    }
}

@Entity(tableName = "delivery_items")
data class DeliveryItemEntity(
    @PrimaryKey val id: String,
    val delivery_order_id: String,
    val product_id: String,
    val quantity: Int,
    val created_at: String? = null
) {
    fun toDomainModel(): DeliveryItem {
        return DeliveryItem(
            id = id,
            delivery_order_id = delivery_order_id,
            product_id = product_id,
            quantity = quantity,
            created_at = created_at
        )
    }
}
