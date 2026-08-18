package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfilesByRole(role: String): Flow<List<Profile>>
    suspend fun getProfileById(id: String): Profile?
    suspend fun createDeliveryUser(email: String, password: String, firstName: String, lastName: String): Result<String>
    suspend fun updateProfile(profile: Profile)
    suspend fun deleteProfile(id: String)
}
