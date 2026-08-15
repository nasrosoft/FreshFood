package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadataEntity(
    @PrimaryKey val entity_type: String, // e.g., "products", "customers"
    var last_sync_at: String? = null, // Timestamp of the last successful sync
    var last_successful_sync: Long = System.currentTimeMillis(),
    var sync_status: String = "IDLE" // IDLE, SYNCING, ERROR
)
