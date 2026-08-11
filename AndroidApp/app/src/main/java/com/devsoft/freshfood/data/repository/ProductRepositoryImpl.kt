package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.StockBatch
import com.devsoft.freshfood.domain.repository.ProductRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProductRepositoryImpl(
    private val supabase: SupabaseClient
) : ProductRepository {

    override suspend fun getProducts(): Flow<List<Product>> = flow {
        // Fetch all active products
        val products = supabase.postgrest["products"]
            .select()
            .decodeList<Product>()
        emit(products)
    }

    override suspend fun getProductById(id: String): Product? {
        return supabase.postgrest["products"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<Product>()
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
        // Fetch stock batches where quantity > 0 ordered by expiration date (FEFO preview)
        return supabase.postgrest["stock_batches"]
            .select {
                filter {
                    eq("product_id", productId)
                    gt("quantity", 0)
                }
            }
            .decodeList<StockBatch>()
            .sortedBy { it.expiration_date }
    }
}
