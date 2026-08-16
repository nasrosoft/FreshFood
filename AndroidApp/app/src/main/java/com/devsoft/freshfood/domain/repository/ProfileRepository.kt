package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getProfilesByRole(role: String): Flow<List<ProfileEntity>>
    suspend fun getProfileById(id: String): ProfileEntity?
    suspend fun createDeliveryUser(email: String, password: String, firstName: String, lastName: String): Result<String>
}
