package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryItem(
    val id: String,
    val delivery_order_id: String,
    val product_id: String,
    val quantity: Int,
    val created_at: String? = null
)
