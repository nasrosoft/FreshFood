import os

base_dir = r'd:\github\FreshFood\AndroidApp\app\src\main\java\com\freshfood\app\domain\model'
os.makedirs(base_dir, exist_ok=True)

models = {
    'Category.kt': '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String? = null
)
''',
    'Brand.kt': '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Brand(
    val id: String,
    val name: String,
    val description: String? = null
)
''',
    'Product.kt': '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String,
    val barcode: String? = null,
    val name: String,
    val category_id: String? = null,
    val brand_id: String? = null,
    val description: String? = null,
    val image_url: String? = null,
    val unit: String = "Unit",
    val purchase_price: Double = 0.0,
    val selling_price: Double = 0.0,
    val min_selling_price: Double = 0.0,
    val current_stock: Int = 0,
    val min_stock: Int = 0,
    val max_stock: Int? = null,
    val is_active: Boolean = true
)
''',
    'StockBatch.kt': '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StockBatch(
    val id: String,
    val product_id: String,
    val quantity: Int,
    val expiration_date: String // Stored as ISO string YYYY-MM-DD
)
'''
}

for filename, content in models.items():
    with open(os.path.join(base_dir, filename), 'w', encoding='utf-8') as f:
        f.write(content)

repo_dir = r'd:\github\FreshFood\AndroidApp\app\src\main\java\com\freshfood\app\domain\repository'
os.makedirs(repo_dir, exist_ok=True)

repo_content = '''package com.freshfood.app.domain.repository

import com.freshfood.app.domain.model.Product
import com.freshfood.app.domain.model.StockBatch
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun getProducts(): Flow<List<Product>>
    suspend fun getProductById(id: String): Product?
    suspend fun insertProduct(product: Product)
    suspend fun updateProduct(product: Product)
    suspend fun getStockBatchesForProduct(productId: String): List<StockBatch>
}
'''
with open(os.path.join(repo_dir, 'ProductRepository.kt'), 'w', encoding='utf-8') as f:
    f.write(repo_content)

print('Models and Repositories for Phase 3 created successfully.')
