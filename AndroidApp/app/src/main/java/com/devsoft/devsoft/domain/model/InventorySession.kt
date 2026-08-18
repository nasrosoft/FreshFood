package com.devsoft.devsoft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InventorySession(
    val id: String,
    val date: String? = null,
    val status: String = "OPEN",
    val conducted_by: String? = null,
    val notes: String? = null
)
