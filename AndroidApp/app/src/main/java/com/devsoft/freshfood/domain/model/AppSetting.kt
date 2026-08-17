package com.devsoft.freshfood.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSetting(
    val id: Int = 1,
    val app_enabled: Int = 1
)
