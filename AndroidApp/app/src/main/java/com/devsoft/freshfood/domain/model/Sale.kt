package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String? = null,
    val invoice_number: String? = null,
    val customer_id: String? = null,
    val user_id: String? = null,
    val total_amount: Double,
    val paid_amount: Double,
    val credit_amount: Double,
    val payment_method: String,
    val status: String = "COMPLETED",
    val created_at: String? = null
)
