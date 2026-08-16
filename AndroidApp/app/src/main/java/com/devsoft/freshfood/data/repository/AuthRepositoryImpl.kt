package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email

class AuthRepositoryImpl(
    private val supabase: SupabaseClient
) : AuthRepository {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            supabase.auth.signOut()
        } catch (e: Exception) {
            // Ignore failure
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }
}
