package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.StockBatch

@Entity(tableName = "stock_batches")
data class StockBatchEntity(
    @PrimaryKey val id: String,
    val product_id: String,
    val batch_number: String? = null,
    val expiration_date: String? = null,
    val quantity: Int,
    val purchase_price: Double,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    fun toDomainModel(): StockBatch {
        return StockBatch(
            id = id,
            product_id = product_id,
            quantity = quantity,
            expiration_date = expiration_date ?: ""
        )
    }

    companion object {
        fun fromDomainModel(batch: StockBatch): StockBatchEntity {
            return StockBatchEntity(
                id = batch.id,
                product_id = batch.product_id,
                expiration_date = batch.expiration_date,
                quantity = batch.quantity,
                purchase_price = 0.0,
                created_at = null,
                updated_at = null
            )
        }
    }
}

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey val id: String,
    val product_id: String,
    val batch_id: String? = null,
    val movement_type: String, // SALE, PURCHASE, RETURN, ADJUSTMENT
    val quantity: Int, // Positive for IN, Negative for OUT
    val reference_id: String? = null, // e.g., sale_id, purchase_id
    val user_id: String,
    val notes: String? = null,
    val created_at: String? = null
)
