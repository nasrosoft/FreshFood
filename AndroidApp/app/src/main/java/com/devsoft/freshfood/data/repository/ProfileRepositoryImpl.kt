package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.Profile
import com.devsoft.freshfood.domain.repository.ProfileRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ProfileRepositoryImpl(
    private val supabase: SupabaseClient
) : ProfileRepository {
    override fun getProfilesByRole(role: String): Flow<List<Profile>> = flow {
        try {
            val profiles = supabase.postgrest["profiles"]
                .select { filter { eq("role", role) } }
                .decodeList<Profile>()
            emit(profiles)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getProfileById(id: String): Profile? {
        return try {
            supabase.postgrest["profiles"]
                .select { filter { eq("id", id) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) {
            null
        }
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
            val responseJson = kotlinx.serialization.json.Json.parseToJsonElement(responseStr).jsonObject
            val newUserId = responseJson["user_id"]?.jsonPrimitive?.content ?: ""
            Result.success(newUserId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(profile: Profile) {
        try {
            supabase.postgrest["profiles"].update(profile) {
                filter { eq("id", profile.id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteProfile(id: String) {
        try {
            supabase.postgrest["profiles"].delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
