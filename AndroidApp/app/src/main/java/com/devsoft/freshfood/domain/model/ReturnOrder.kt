package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ReturnOrder(
    val id: String,
    val date: String? = null,
    val customer_id: String? = null,
    val product_id: String,
    val quantity: Int,
    val reason: String? = null,
    val status: String = "PENDING",
    val created_by: String? = null
)
