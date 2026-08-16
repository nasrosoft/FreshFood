package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val role: String,
    val first_name: String? = null,
    val last_name: String? = null,
    val email: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
)
