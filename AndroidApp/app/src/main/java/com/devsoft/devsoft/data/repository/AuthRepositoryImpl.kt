package com.devsoft.devsoft.data.repository

import com.devsoft.devsoft.domain.repository.AuthRepository
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

    override suspend fun restoreSession(): Result<String?> {
        return try {
            supabase.auth.awaitInitialization()
            val currentSession = supabase.auth.currentSessionOrNull()
            if (currentSession != null) {
                try {
                    supabase.auth.refreshCurrentSession()
                } catch (e: Exception) {
                    // Ignore network failure on refresh, use cached session
                }
                val userId = supabase.auth.currentSessionOrNull()?.user?.id ?: currentSession.user?.id
                Result.success(userId)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    override fun getCurrentUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }
}
