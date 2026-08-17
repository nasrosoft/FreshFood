package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.AppSetting
import com.devsoft.freshfood.domain.repository.ActivationRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
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
