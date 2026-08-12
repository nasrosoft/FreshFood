package com.devsoft.freshfood.domain.model

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
    val id: String? = null,
    val purchase_id: String? = null,
    val product_id: String,
    val batch_id: String? = null,
    val quantity: Int,
    val purchase_price: Double,
    val expiration_date: String
)

@Serializable
data class PurchaseRequest(
    val supplier_id: String? = null,
    val invoice_number: String? = null,
    val user_id: String,
    val items: List<PurchaseItem>
)
