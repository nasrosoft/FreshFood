package com.devsoft.devsoft.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun logout()
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
}
