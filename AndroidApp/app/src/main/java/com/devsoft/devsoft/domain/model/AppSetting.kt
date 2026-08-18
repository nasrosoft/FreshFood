package com.devsoft.devsoft.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSetting(
    val id: Int = 1,
    val app_enabled: Int = 1,
    val brand_name: String? = "DevSoft",
    val brand_tagline: String? = "Stock & Sales Management"
)
