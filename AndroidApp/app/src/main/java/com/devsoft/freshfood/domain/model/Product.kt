package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val barcode: String? = null,
    val name: String,
    val category_id: String? = null,
    val brand_id: String? = null,
    val description: String? = null,
    val image_url: String? = null,
    val unit: String = "Unit",
    val purchase_price: Double = 0.0,
    val selling_price: Double = 0.0,
    val min_selling_price: Double = 0.0,
    val current_stock: Int = 0,
    val min_stock: Int = 0,
    val max_stock: Int? = null,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)
