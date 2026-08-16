package com.devsoft.freshfood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val first_name: String,
    val last_name: String,
    val phone: String?,
    val role: String,
    val is_active: Boolean,
    val created_at: String,
    val updated_at: String
)
