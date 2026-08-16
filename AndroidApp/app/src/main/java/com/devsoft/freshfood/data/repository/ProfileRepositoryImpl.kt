package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.data.local.FreshFoodDatabase
import com.devsoft.freshfood.data.local.entity.ProfileEntity
import com.devsoft.freshfood.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProfileRepositoryImpl(
    private val database: FreshFoodDatabase,
    private val supabase: SupabaseClient
) : ProfileRepository {
    override fun getProfilesByRole(role: String): Flow<List<ProfileEntity>> {
        return database.profileDao().getProfilesByRole(role)
    }

    override suspend fun getProfileById(id: String): ProfileEntity? {
        return database.profileDao().getProfileById(id)
    }

    override suspend fun createDeliveryUser(email: String, password: String, firstName: String, lastName: String): Result<String> {
        return try {
            val adminUid = supabase.auth.currentSessionOrNull()?.user?.id 
                ?: return Result.failure(Exception("Admin not logged in"))
                
            val payload = buildJsonObject {
                put("admin_uid", adminUid)
                put("user_email", email)
                put("user_password", password)
                put("user_first_name", firstName)
                put("user_last_name", lastName)
            }
            
            val responseStr = supabase.postgrest.rpc("create_delivery_user", payload).data
            // responseStr is a JSON string like {"success": true, "user_id": "uuid"}
            // So we parse it manually since we don't have a data class for it
            val responseJson = kotlinx.serialization.json.Json.parseToJsonElement(responseStr).jsonObject
            val newUserId = responseJson["user_id"]?.jsonPrimitive?.content ?: ""
            Result.success(newUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
