package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryItem(
    val id: String,
    val session_id: String,
    val product_id: String,
    val expected_quantity: Int,
    val actual_quantity: Int,
    val difference: Int? = null
)
