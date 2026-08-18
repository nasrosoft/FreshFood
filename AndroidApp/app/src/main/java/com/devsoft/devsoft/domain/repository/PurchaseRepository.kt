package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.PurchaseRequest

interface PurchaseRepository {
    suspend fun processPurchase(request: PurchaseRequest): Result<String>
}
