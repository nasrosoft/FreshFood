package com.devsoft.freshfood.presentation.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProductsViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            repository.getProducts()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(error = e.message ?: "Unknown Error", isLoading = false)
                }
                .collect { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        filteredProducts = filterProducts(products, _uiState.value.searchQuery),
                        isLoading = false
                    )
                }
        }
    }

    fun updateSearchQuery(query: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            searchQuery = query,
            filteredProducts = filterProducts(currentState.products, query)
        )
    }

    private fun filterProducts(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products
        return products.filter {
            it.name.contains(query, ignoreCase = true) || 
            it.barcode?.contains(query, ignoreCase = true) == true
        }
    }

    fun findProductByBarcode(barcode: String): Product? {
        return _uiState.value.products.find { it.barcode == barcode }
    }

    suspend fun searchProductByBarcode(barcode: String): Product? {
        val trimmed = barcode.trim()
        if (trimmed.isBlank()) return null
        val local = _uiState.value.products.find { it.barcode.equals(trimmed, ignoreCase = true) }
        if (local != null) return local
        return try {
            val remote = repository.getProductByBarcode(trimmed)
            if (remote != null) {
                loadProducts()
            }
            remote
        } catch (e: Exception) {
            null
        }
    }

    fun addStock(product: Product, quantityToAdd: Int, expirationDate: String) {
        viewModelScope.launch {
            // Update product's total current_stock
            val updatedProduct = product.copy(current_stock = product.current_stock + quantityToAdd)
            repository.updateProduct(updatedProduct)
            
            // Insert a new StockBatch
            val stockBatch = com.devsoft.freshfood.domain.model.StockBatch(
                id = java.util.UUID.randomUUID().toString(),
                product_id = product.id,
                quantity = quantityToAdd,
                expiration_date = expirationDate
            )
            repository.insertStockBatch(stockBatch)
            
            // Reload to reflect changes
            loadProducts()
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.insertProduct(product)
                loadProducts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to add product")
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.updateProduct(product)
                loadProducts()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to update product")
            }
        }
    }

    suspend fun getLotCount(productId: String): Int {
        return try {
            repository.getStockBatchesForProduct(productId).size
        } catch (e: Exception) {
            0
        }
    }
}

class ProductsViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
