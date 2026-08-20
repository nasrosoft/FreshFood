package com.devsoft.devsoft.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
    suspend fun restoreSession(): Result<String?>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}
