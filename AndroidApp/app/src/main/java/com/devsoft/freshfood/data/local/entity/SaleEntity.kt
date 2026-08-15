package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.Sale
import kotlinx.serialization.Serializable

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val invoice_number: String? = null,
    val customer_id: String? = null,
    val user_id: String? = null,
    val total_amount: Double,
    val paid_amount: Double,
    val credit_amount: Double,
    val payment_method: String = "CASH",
    val status: String = "COMPLETED",
    val deleted_at: String? = null
) {
    fun toDomainModel(): Sale {
        return Sale(
            id = id,
            invoice_number = invoice_number,
            customer_id = customer_id,
            user_id = user_id,
            total_amount = total_amount,
            paid_amount = paid_amount,
            credit_amount = credit_amount,
            payment_method = payment_method,
            status = status
        )
    }

    companion object {
        fun fromDomainModel(sale: Sale, deletedAt: String? = null): SaleEntity {
            return SaleEntity(
                id = sale.id ?: java.util.UUID.randomUUID().toString(),
                invoice_number = sale.invoice_number,
                customer_id = sale.customer_id,
                user_id = sale.user_id,
                total_amount = sale.total_amount,
                paid_amount = sale.paid_amount,
                credit_amount = sale.credit_amount,
                payment_method = sale.payment_method,
                status = sale.status,
                deleted_at = deletedAt
            )
        }
    }
}

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey val id: String,
    val sale_id: String,
    val product_id: String,
    val quantity: Int,
    val unit_price: Double,
    val subtotal: Double,
    val created_at: String? = null
)
