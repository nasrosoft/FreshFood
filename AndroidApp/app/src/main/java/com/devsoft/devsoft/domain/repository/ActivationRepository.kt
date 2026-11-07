package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.AppSetting

interface ActivationRepository {
    suspend fun isAppEnabled(): Result<Boolean>
    suspend fun getAppSettings(): Result<AppSetting>
    suspend fun updateBrandSettings(brandName: String, brandTagline: String): Result<Unit>

    /** Registers this phone with the master station and returns its access (status + modules). */
    suspend fun registerDevice(deviceId: String, deviceName: String, appVersion: String): Result<com.devsoft.devsoft.domain.model.DeviceRegistration>
}
