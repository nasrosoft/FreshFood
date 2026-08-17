package com.devsoft.freshfood.domain.repository

interface ActivationRepository {
    suspend fun isAppEnabled(): Result<Boolean>
}
