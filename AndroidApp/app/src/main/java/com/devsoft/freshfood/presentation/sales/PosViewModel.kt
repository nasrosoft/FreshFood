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
    val checkoutMessage: String? = null
)

class PosViewModel(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    // Temporary mock user_id for Phase 4 (Will be fetched from Auth in production)
    private val MOCK_USER_ID = "00000000-0000-0000-0000-000000000000"

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

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList(), totalAmount = 0.0, checkoutMessage = null) }
    }

    private fun calculateTotal(items: List<CartItem>): Double {
        return items.sumOf { it.product.selling_price * it.quantity }
    }

    fun checkout(paymentMethod: String) {
        val currentState = _uiState.value
        if (currentState.cartItems.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true, checkoutMessage = null) }

        viewModelScope.launch {
            val itemsReq = currentState.cartItems.map {
                SaleItemRequest(
                    product_id = it.product.id,
                    quantity = it.quantity,
                    unit_price = it.product.selling_price
                )
            }
            
            val saleReq = SaleRequest(
                customer_id = null, // Guest checkout for now
                user_id = MOCK_USER_ID, 
                total_amount = currentState.totalAmount,
                paid_amount = currentState.totalAmount, // Assuming full payment in CASH/CARD
                credit_amount = 0.0,
                payment_method = paymentMethod,
                items = itemsReq
            )

            val result = salesRepository.processSale(saleReq)
            
            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        isLoading = false,
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
