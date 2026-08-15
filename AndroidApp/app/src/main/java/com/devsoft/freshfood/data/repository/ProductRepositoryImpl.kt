package com.devsoft.freshfood.data.repository

import android.content.Context
import com.devsoft.freshfood.data.local.dao.ProductDao
import com.devsoft.freshfood.data.local.dao.SyncQueueDao
import com.devsoft.freshfood.data.local.entity.ProductEntity
import com.devsoft.freshfood.data.local.entity.SyncQueueEntity
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.StockBatch
import com.devsoft.freshfood.domain.repository.ProductRepository
import com.devsoft.freshfood.utils.DeviceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val syncQueueDao: SyncQueueDao,
    private val context: Context
) : ProductRepository {

    override suspend fun getProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities -> 
            entities.map { it.toDomainModel() } 
        }
    }

    override suspend fun getProductById(id: String): Product? {
        return productDao.getProductById(id)?.toDomainModel()
    }

    override suspend fun insertProduct(product: Product) {
        val entity = ProductEntity.fromDomainModel(product)
        productDao.insertProduct(entity)
        
        val syncQueueEntity = SyncQueueEntity(
            entity_type = "products",
            entity_id = product.id,
            operation = "CREATE",
            payload = Json.encodeToString(product),
            device_id = DeviceUtil.getDeviceId(context)
        )
        syncQueueDao.insert(syncQueueEntity)
    }

    override suspend fun updateProduct(product: Product) {
        val entity = ProductEntity.fromDomainModel(product)
        productDao.updateProduct(entity)

        val syncQueueEntity = SyncQueueEntity(
            entity_type = "products",
            entity_id = product.id,
            operation = "UPDATE",
            payload = Json.encodeToString(product),
            device_id = DeviceUtil.getDeviceId(context)
        )
        syncQueueDao.insert(syncQueueEntity)
    }

    override suspend fun getStockBatchesForProduct(productId: String): List<StockBatch> {
        // TODO: Implement StockBatchDao and use it here
        return emptyList()
    }

    override suspend fun insertStockBatch(stockBatch: StockBatch) {
        // TODO: Implement StockBatchDao and use it here
    }
}
