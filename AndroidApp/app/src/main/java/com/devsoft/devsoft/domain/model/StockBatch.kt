package com.devsoft.devsoft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StockBatch(
    val id: String,
    val product_id: String,
    val quantity: Int,
    val expiration_date: String // Stored as ISO string YYYY-MM-DD
)
