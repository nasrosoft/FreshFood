package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String? = null,
    val customer_id: String,
    val amount: Double,
    val payment_method: String,
    val reference_id: String? = null,
    val user_id: String,
    val notes: String? = null
)
