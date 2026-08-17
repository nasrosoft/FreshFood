package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.domain.model.AppSetting

interface ActivationRepository {
    suspend fun isAppEnabled(): Result<Boolean>
    suspend fun getAppSettings(): Result<AppSetting>
    suspend fun updateBrandSettings(brandName: String, brandTagline: String): Result<Unit>
}
