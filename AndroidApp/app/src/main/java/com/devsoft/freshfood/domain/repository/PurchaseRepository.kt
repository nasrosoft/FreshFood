package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.domain.model.PurchaseRequest

interface PurchaseRepository {
    suspend fun processPurchase(request: PurchaseRequest): Result<String>
}
