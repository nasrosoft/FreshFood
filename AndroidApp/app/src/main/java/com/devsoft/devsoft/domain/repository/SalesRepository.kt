package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.SaleRequest

interface SalesRepository {
    suspend fun processSale(saleRequest: SaleRequest): Result<String>
}
