package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val wilaya: String? = null,
    val commune: String? = null,
    val photo_url: String? = null,
    val credit_limit: Double = 0.0,
    val current_credit: Double = 0.0,
    val customer_type: String? = null,
    val is_active: Boolean = true,
    val notes: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
