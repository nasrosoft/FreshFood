package com.devsoft.freshfood.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.SaleItemRequest
import com.devsoft.freshfood.domain.model.SaleRequest
import com.devsoft.freshfood.domain.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    var quantity: Int
)

data class PosUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val checkoutMessage: String? = null,
    val lastSaleItems: List<CartItem>? = null,
    val lastSaleTotal: Double? = null
)

class PosViewModel(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    fun addToCart(product: Product) {
        _uiState.update { state ->
            val existingItem = state.cartItems.find { it.product.id == product.id }
            val newItems = if (existingItem != null) {
                state.cartItems.map { 
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.cartItems + CartItem(product, 1)
            }
            state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
        }
    }

    fun decreaseQuantity(product: Product) {
        _uiState.update { state ->
            val existingItem = state.cartItems.find { it.product.id == product.id }
            if (existingItem != null) {
                val newItems = if (existingItem.quantity > 1) {
                    state.cartItems.map {
                        if (it.product.id == product.id) it.copy(quantity = it.quantity - 1) else it
                    }
                } else {
                    state.cartItems.filter { it.product.id != product.id }
                }
                state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
            } else {
                state
            }
        }
    }
    fun setQuantity(product: Product, quantity: Int) {
        if (quantity <= 0) {
            removeItem(product)
            return
        }
        _uiState.update { state ->
            val existingItem = state.cartItems.find { it.product.id == product.id }
            if (existingItem != null) {
                val newItems = state.cartItems.map {
                    if (it.product.id == product.id) it.copy(quantity = quantity) else it
                }
                state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
            } else {
                state
            }
        }
    }

    fun removeItem(product: Product) {
        _uiState.update { state ->
            val newItems = state.cartItems.filter { it.product.id != product.id }
            state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList(), totalAmount = 0.0, checkoutMessage = null, lastSaleItems = null, lastSaleTotal = null) }
    }

    fun dismissCheckoutMessage() {
        _uiState.update { it.copy(checkoutMessage = null, lastSaleItems = null, lastSaleTotal = null) }
    }

    private fun calculateTotal(items: List<CartItem>): Double {
        return items.sumOf { it.product.selling_price * it.quantity }
    }
    fun checkout(paymentMethod: String, customerId: String? = null, createDelivery: Boolean = false) {
        val currentState = _uiState.value
        if (currentState.cartItems.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true, checkoutMessage = null) }

        viewModelScope.launch {
            val itemsReq = currentState.cartItems.map {
                SaleItemRequest(
                    id = java.util.UUID.randomUUID().toString(),
                    product_id = it.product.id,
                    quantity = it.quantity,
                    unit_price = it.product.selling_price
                )
            }
            
            val saleReq = SaleRequest(
                id = java.util.UUID.randomUUID().toString(),
                customer_id = customerId,
                user_id = null, // Authentication not yet implemented
                total_amount = currentState.totalAmount,
                paid_amount = currentState.totalAmount, // Assuming full payment in CASH/CARD
                credit_amount = 0.0,
                payment_method = paymentMethod,
                items = itemsReq,
                create_delivery = createDelivery
            )

            val result = salesRepository.processSale(saleReq)
            
            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        isLoading = false,
                        lastSaleItems = state.cartItems,
                        lastSaleTotal = state.totalAmount,
                        cartItems = emptyList(),
                        totalAmount = 0.0,
                        checkoutMessage = "Success! Invoice: ${result.getOrNull()}"
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        checkoutMessage = "Failed: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
}

class PosViewModelFactory(
    private val salesRepository: SalesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(salesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
