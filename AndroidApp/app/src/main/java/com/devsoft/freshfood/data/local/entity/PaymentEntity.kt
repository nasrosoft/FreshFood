package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.Payment

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey val id: String,
    val customer_id: String,
    val amount: Double,
    val payment_method: String,
    val reference_id: String? = null,
    val user_id: String,
    val notes: String? = null,
    val created_at: String? = null,
    val deleted_at: String? = null
) {
    fun toDomainModel(): Payment {
        return Payment(
            id = id,
            customer_id = customer_id,
            amount = amount,
            payment_method = payment_method,
            reference_id = reference_id,
            user_id = user_id,
            notes = notes
        )
    }

    companion object {
        fun fromDomainModel(payment: Payment, deletedAt: String? = null): PaymentEntity {
            return PaymentEntity(
                id = payment.id ?: java.util.UUID.randomUUID().toString(),
                customer_id = payment.customer_id,
                amount = payment.amount,
                payment_method = payment.payment_method,
                reference_id = payment.reference_id,
                user_id = payment.user_id,
                notes = payment.notes,
                created_at = null,
                deleted_at = deletedAt
            )
        }
    }
}

@Entity(tableName = "credit_transactions")
data class CreditTransactionEntity(
    @PrimaryKey val id: String,
    val customer_id: String,
    val amount: Double,
    val transaction_type: String, // DEBT, PAYMENT
    val reference_id: String? = null,
    val user_id: String,
    val created_at: String? = null,
    val deleted_at: String? = null
)
