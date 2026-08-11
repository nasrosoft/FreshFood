package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryOrder(
    val id: String,
    val customer_id: String? = null,
    val delivery_employee_id: String? = null,
    val status: String = "PENDING",
    val notes: String? = null
)
