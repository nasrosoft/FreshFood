package com.devsoft.devsoft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Purchase(
    val id: String? = null,
    val supplier_id: String? = null,
    val invoice_number: String? = null,
    val total_amount: Double = 0.0,
    val status: String = "COMPLETED",
    val created_at: String? = null
)

@Serializable
data class PurchaseItem(
    val id: String? = null, // Can be generated locally
    val purchase_id: String? = null,
    val product_id: String,
    val batch_id: String? = null,
    val quantity: Int,
    val purchase_price: Double,
    val expiration_date: String
)

@Serializable
data class PurchaseRequest(
    val id: String, // Added UUID for idempotency
    val supplier_id: String? = null,
    val invoice_number: String? = null,
    val user_id: String,
    val total_amount: Double,
    val items: List<PurchaseItem>,
    val created_at: String? = null
)
