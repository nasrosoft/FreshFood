package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleRequest(
    val customer_id: String? = null,
    val user_id: String? = null,
    val total_amount: Double,
    val paid_amount: Double,
    val credit_amount: Double,
    val payment_method: String,
    val items: List<SaleItemRequest>,
    val create_delivery: Boolean = false
)

@Serializable
data class SaleItemRequest(
    val product_id: String,
    val quantity: Int,
    val unit_price: Double
)
