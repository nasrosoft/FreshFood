package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devsoft.freshfood.domain.model.InventoryItem
import com.devsoft.freshfood.domain.model.InventorySession

@Entity(tableName = "inventory_sessions")
data class InventorySessionEntity(
    @PrimaryKey val id: String,
    val date: String? = null,
    val status: String = "OPEN",
    val conducted_by: String? = null,
    val notes: String? = null
) {
    fun toDomainModel(): InventorySession {
        return InventorySession(
            id = id,
            date = date,
            status = status,
            conducted_by = conducted_by,
            notes = notes
        )
    }
}

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val session_id: String,
    val product_id: String,
    val expected_quantity: Int,
    val actual_quantity: Int,
    val difference: Int? = null
) {
    fun toDomainModel(): InventoryItem {
        return InventoryItem(
            id = id,
            session_id = session_id,
            product_id = product_id,
            expected_quantity = expected_quantity,
            actual_quantity = actual_quantity,
            difference = difference
        )
    }
}
