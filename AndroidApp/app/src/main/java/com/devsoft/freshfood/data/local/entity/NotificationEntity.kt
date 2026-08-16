package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val user_id: String,
    val title: String,
    val message: String,
    val is_read: Boolean,
    val created_at: String
)
