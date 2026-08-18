package com.devsoft.devsoft.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.devsoft.domain.model.Product
import com.devsoft.devsoft.domain.model.SaleItemRequest
import com.devsoft.devsoft.domain.model.SaleRequest
import com.devsoft.devsoft.domain.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.devsoft.devsoft.domain.repository.ProfileRepository
import com.devsoft.devsoft.domain.model.Profile

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
    val lastSaleTotal: Double? = null,
    val deliveryDrivers: List<Profile> = emptyList()
)

class PosViewModel(
    private val salesRepository: SalesRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _cartState = MutableStateFlow(PosUiState())
    
    val uiState: StateFlow<PosUiState> = combine(
        _cartState,
        profileRepository.getProfilesByRole("DELIVERY")
    ) { state, drivers ->
        state.copy(deliveryDrivers = drivers)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PosUiState()
    )

    fun addToCart(product: Product) {
        _cartState.update { state ->
            val existingItem = state.cartItems.find { it.product.id == product.id }
            val newItems = if (existingItem != null) {
                if (existingItem.quantity >= product.current_stock) {
                    return@update state // Max stock reached
                }
                state.cartItems.map { 
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                if (product.current_stock > 0) {
                    state.cartItems + CartItem(product, 1)
                } else {
                    return@update state // Out of stock
                }
            }
            state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
        }
    }

    fun decreaseQuantity(product: Product) {
        _cartState.update { state ->
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
        val maxQuantity = if (quantity > product.current_stock) product.current_stock else quantity
        _cartState.update { state ->
            val existingItem = state.cartItems.find { it.product.id == product.id }
            if (existingItem != null) {
                val newItems = state.cartItems.map {
                    if (it.product.id == product.id) it.copy(quantity = maxQuantity) else it
                }
                state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
            } else {
                state
            }
        }
    }

    fun removeItem(product: Product) {
        _cartState.update { state ->
            val newItems = state.cartItems.filter { it.product.id != product.id }
            state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
        }
    }

    fun clearCart() {
        _cartState.update { it.copy(cartItems = emptyList(), totalAmount = 0.0, checkoutMessage = null, lastSaleItems = null, lastSaleTotal = null) }
    }

    fun dismissCheckoutMessage() {
        _cartState.update { it.copy(checkoutMessage = null, lastSaleItems = null, lastSaleTotal = null) }
    }

    private fun calculateTotal(items: List<CartItem>): Double {
        return items.sumOf { it.product.selling_price * it.quantity }
    }

    fun checkout(paymentMethod: String, customerId: String? = null, createDelivery: Boolean = false, deliveryDriverId: String? = null) {
        val currentState = _cartState.value
        if (currentState.cartItems.isEmpty()) return
        
        val totalSaleAmount = currentState.totalAmount
        val currentSaleItems = currentState.cartItems.toList()
        
        _cartState.update { it.copy(isLoading = true, checkoutMessage = null) }

        viewModelScope.launch {
            val itemsReq = currentSaleItems.map {
                SaleItemRequest(
                    id = java.util.UUID.randomUUID().toString(),
                    product_id = it.product.id,
                    quantity = it.quantity,
                    unit_price = it.product.selling_price
                )
            }
            
            val isCredit = paymentMethod.equals("CREDIT", ignoreCase = true)
            val paidAmount = if (isCredit) 0.0 else totalSaleAmount
            val creditAmount = if (isCredit) totalSaleAmount else 0.0

            val saleReq = SaleRequest(
                id = java.util.UUID.randomUUID().toString(),
                customer_id = customerId,
                user_id = null,
                total_amount = totalSaleAmount,
                paid_amount = paidAmount,
                credit_amount = creditAmount,
                payment_method = paymentMethod,
                items = itemsReq,
                create_delivery = createDelivery,
                delivery_employee_id = deliveryDriverId
            )

            val result = salesRepository.processSale(saleReq)
            
            _cartState.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        isLoading = false,
                        lastSaleItems = currentSaleItems,
                        lastSaleTotal = totalSaleAmount,
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
    private val salesRepository: SalesRepository,
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(salesRepository, profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
