package com.devsoft.devsoft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomerCreditDetail(
    val id: String,
    val customer_id: String,
    val sale_id: String? = null,
    val invoice_number: String? = null,
    val items_summary: String? = null,
    val total_amount: Double = 0.0,
    val credit_amount: Double = 0.0,
    val transaction_type: String = "CREDIT_SALE",
    val created_at: String? = null
)
