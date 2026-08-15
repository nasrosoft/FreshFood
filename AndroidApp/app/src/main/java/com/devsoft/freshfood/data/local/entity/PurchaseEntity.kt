package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.Purchase

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val supplier_id: String? = null,
    val invoice_number: String? = null,
    val total_amount: Double = 0.0,
    val status: String = "COMPLETED",
    val created_at: String? = null
) {
    fun toDomainModel(): Purchase {
        return Purchase(
            id = id,
            supplier_id = supplier_id,
            invoice_number = invoice_number,
            total_amount = total_amount,
            status = status,
            created_at = created_at
        )
    }
}

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey val id: String,
    val purchase_id: String,
    val product_id: String,
    val batch_id: String? = null,
    val quantity: Int,
    val purchase_price: Double,
    val expiration_date: String
)
