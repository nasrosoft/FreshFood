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
                // If table exists but empty, default to blocked until explicitly enabled
                Result.success(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
