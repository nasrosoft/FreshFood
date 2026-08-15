package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
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
    val updated_at: String? = null,
    val deleted_at: String? = null // For soft delete logic
) {
    fun toDomainModel(): Product {
        return Product(
            id = id,
            barcode = barcode,
            name = name,
            category_id = category_id,
            brand_id = brand_id,
            description = description,
            image_url = image_url,
            unit = unit,
            purchase_price = purchase_price,
            selling_price = selling_price,
            min_selling_price = min_selling_price,
            current_stock = current_stock,
            min_stock = min_stock,
            max_stock = max_stock,
            is_active = is_active,
            created_at = created_at,
            updated_at = updated_at
        )
    }

    companion object {
        fun fromDomainModel(product: Product, deletedAt: String? = null): ProductEntity {
            return ProductEntity(
                id = product.id,
                barcode = product.barcode,
                name = product.name,
                category_id = product.category_id,
                brand_id = product.brand_id,
                description = product.description,
                image_url = product.image_url,
                unit = product.unit,
                purchase_price = product.purchase_price,
                selling_price = product.selling_price,
                min_selling_price = product.min_selling_price,
                current_stock = product.current_stock,
                min_stock = product.min_stock,
                max_stock = product.max_stock,
                is_active = product.is_active,
                created_at = product.created_at,
                updated_at = product.updated_at,
                deleted_at = deletedAt
            )
        }
    }
}
