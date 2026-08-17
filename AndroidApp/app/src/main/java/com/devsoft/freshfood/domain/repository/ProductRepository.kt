package com.devsoft.freshfood.domain.repository

import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.StockBatch
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun insertProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun getStockBatchesForProduct(productId: String): List<StockBatch>
    suspend fun insertStockBatch(stockBatch: StockBatch)
}
