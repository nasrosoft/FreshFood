package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Brand(
    val id: String,
    val name: String,
    val description: String? = null
)
