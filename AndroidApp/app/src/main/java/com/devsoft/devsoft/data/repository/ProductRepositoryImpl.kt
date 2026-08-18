package com.devsoft.devsoft.data.repository

import com.devsoft.devsoft.domain.model.Product
import com.devsoft.devsoft.domain.model.StockBatch
import com.devsoft.devsoft.domain.repository.ProductRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepositoryImpl(
    private val supabase: SupabaseClient
) : ProductRepository {

    override suspend fun getProducts(): Flow<List<Product>> = flow {
        val products = supabase.postgrest["products"].select().decodeList<Product>()
        emit(products)
    }

    override suspend fun getProductById(id: String): Product? {
        return supabase.postgrest["products"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<Product>()
    }

    override suspend fun getProductByBarcode(barcode: String): Product? {
        return try {
            supabase.postgrest["products"]
                .select { filter { eq("barcode", barcode) } }
                .decodeSingleOrNull<Product>()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun insertProduct(product: Product) {
        supabase.postgrest["products"].insert(product)
    }

    override suspend fun updateProduct(product: Product) {
        supabase.postgrest["products"].update(product) {
            filter { eq("id", product.id) }
        }
    }

    override suspend fun getStockBatchesForProduct(productId: String): List<StockBatch> {
        return supabase.postgrest["stock_batches"]
            .select { filter { eq("product_id", productId) } }
            .decodeList<StockBatch>()
    }

    override suspend fun insertStockBatch(stockBatch: StockBatch) {
        supabase.postgrest["stock_batches"].insert(stockBatch)
    }
}
