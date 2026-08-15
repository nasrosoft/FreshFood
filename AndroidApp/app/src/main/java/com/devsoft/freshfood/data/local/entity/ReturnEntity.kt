package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.ReturnOrder

@Entity(tableName = "returns")
data class ReturnEntity(
    @PrimaryKey val id: String,
    val date: String? = null,
    val customer_id: String? = null,
    val product_id: String,
    val quantity: Int,
    val reason: String? = null,
    val status: String = "PENDING",
    val created_by: String? = null
) {
    fun toDomainModel(): ReturnOrder {
        return ReturnOrder(
            id = id,
            date = date,
            customer_id = customer_id,
            product_id = product_id,
            quantity = quantity,
            reason = reason,
            status = status,
            created_by = created_by
        )
    }
}
