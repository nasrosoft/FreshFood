package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.Customer

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
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
    val updated_at: String? = null,
    val deleted_at: String? = null // For soft delete logic
) {
    fun toDomainModel(): Customer {
        return Customer(
            id = id,
            name = name,
            phone = phone,
            address = address,
            wilaya = wilaya,
            commune = commune,
            photo_url = photo_url,
            credit_limit = credit_limit,
            current_credit = current_credit,
            customer_type = customer_type,
            is_active = is_active,
            notes = notes,
            created_at = created_at,
            updated_at = updated_at
        )
    }

    companion object {
        fun fromDomainModel(customer: Customer, deletedAt: String? = null): CustomerEntity {
            return CustomerEntity(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                address = customer.address,
                wilaya = customer.wilaya,
                commune = customer.commune,
                photo_url = customer.photo_url,
                credit_limit = customer.credit_limit,
                current_credit = customer.current_credit,
                customer_type = customer.customer_type,
                is_active = customer.is_active,
                notes = customer.notes,
                created_at = customer.created_at,
                updated_at = customer.updated_at,
                deleted_at = deletedAt
            )
        }
    }
}
