package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entity_type: String, // e.g., "PRODUCT", "CUSTOMER", "SALE"
    val entity_id: String, // UUID of the entity
    val operation: String, // "CREATE", "UPDATE", "DELETE"
    val payload: String?, // JSON serialized payload if needed, or null if we just fetch from DB
    val created_at: Long = System.currentTimeMillis(),
    var updated_at: Long = System.currentTimeMillis(),
    var retry_count: Int = 0,
    var last_error: String? = null,
    var status: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    val device_id: String
)
