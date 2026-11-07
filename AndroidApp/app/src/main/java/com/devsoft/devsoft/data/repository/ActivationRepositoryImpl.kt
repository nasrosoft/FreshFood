package com.devsoft.devsoft.data.repository

import com.devsoft.devsoft.domain.model.AppSetting
import com.devsoft.devsoft.domain.repository.ActivationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActivationRepositoryImpl(
    private val supabase: SupabaseClient
) : ActivationRepository {

    override suspend fun isAppEnabled(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val list = supabase.postgrest["app_settings"]
                .select()
                .decodeList<AppSetting>()

            val setting = list.firstOrNull()
            if (setting != null) {
                Result.success(setting.app_enabled == 1)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAppSettings(): Result<AppSetting> = withContext(Dispatchers.IO) {
        try {
            val list = supabase.postgrest["app_settings"]
                .select()
                .decodeList<AppSetting>()
            val setting = list.firstOrNull() ?: AppSetting()
            Result.success(setting)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerDevice(
        deviceId: String,
        deviceName: String,
        appVersion: String
    ): Result<com.devsoft.devsoft.domain.model.DeviceRegistration> = withContext(Dispatchers.IO) {
        try {
            val payload = kotlinx.serialization.json.buildJsonObject {
                put("p_device_id", kotlinx.serialization.json.JsonPrimitive(deviceId))
                put("p_platform", kotlinx.serialization.json.JsonPrimitive("ANDROID"))
                put("p_device_name", kotlinx.serialization.json.JsonPrimitive(deviceName))
                put("p_app_version", kotlinx.serialization.json.JsonPrimitive(appVersion))
            }
            val data = supabase.postgrest.rpc("register_device", payload).data
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true; coerceInputValues = true }
            Result.success(json.decodeFromString(com.devsoft.devsoft.domain.model.DeviceRegistration.serializer(), data))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBrandSettings(brandName: String, brandTagline: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.postgrest["app_settings"].update(
                mapOf(
                    "brand_name" to brandName,
                    "brand_tagline" to brandTagline
                )
            ) {
                filter { eq("id", 1) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
