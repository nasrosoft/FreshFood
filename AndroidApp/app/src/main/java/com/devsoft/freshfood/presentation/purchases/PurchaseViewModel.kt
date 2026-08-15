package com.devsoft.freshfood.presentation.purchases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.PurchaseItem
import com.devsoft.freshfood.domain.model.PurchaseRequest
import com.devsoft.freshfood.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PurchaseUiState(
    val selectedSupplierId: String? = null,
    val invoiceNumber: String = "",
    val items: List<PurchaseItem> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null
)

class PurchaseViewModel(
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    fun updateInvoiceNumber(invoice: String) {
        _uiState.update { it.copy(invoiceNumber = invoice) }
    }

    fun addItem(product: Product, quantity: Int, purchasePrice: Double, expirationDate: String) {
        _uiState.update { state ->
            val newItem = PurchaseItem(
                product_id = product.id,
                quantity = quantity,
                purchase_price = purchasePrice,
                expiration_date = expirationDate
            )
            state.copy(items = state.items + newItem)
        }
    }

    fun submitPurchase() {
        val currentState = _uiState.value
        if (currentState.items.isEmpty()) return

        _uiState.update { it.copy(isLoading = true, message = null) }

        viewModelScope.launch {
            val totalAmount = currentState.items.sumOf { it.quantity * it.purchase_price }
            val request = PurchaseRequest(
                id = java.util.UUID.randomUUID().toString(),
                supplier_id = currentState.selectedSupplierId,
                invoice_number = currentState.invoiceNumber.ifBlank { "INV-${System.currentTimeMillis()}" },
                user_id = "00000000-0000-0000-0000-000000000000", // MOCK USER
                total_amount = totalAmount,
                items = currentState.items
            )

            val result = purchaseRepository.processPurchase(request)
            
            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        isLoading = false,
                        items = emptyList(),
                        invoiceNumber = "",
                        message = "Purchase recorded successfully! ID: ${result.getOrNull()}"
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        message = "Error: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
}

class PurchaseViewModelFactory(
    private val purchaseRepository: PurchaseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PurchaseViewModel(purchaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
