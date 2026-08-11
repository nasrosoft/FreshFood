package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.domain.model.SaleRequest

interface SalesRepository {
    suspend fun processSale(saleRequest: SaleRequest): Result<String>
}
