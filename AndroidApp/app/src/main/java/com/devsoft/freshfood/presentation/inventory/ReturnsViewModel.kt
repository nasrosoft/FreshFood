package com.devsoft.freshfood.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.model.ReturnOrder
import com.devsoft.freshfood.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

sealed class ReturnsUiState {
    object Loading : ReturnsUiState()
    data class Success(val returns: List<ReturnOrder>) : ReturnsUiState()
    data class Error(val message: String) : ReturnsUiState()
}

class ReturnsViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReturnsUiState>(ReturnsUiState.Loading)
    val uiState: StateFlow<ReturnsUiState> = _uiState.asStateFlow()

    init {
        loadReturns()
    }

    fun loadReturns() {
        viewModelScope.launch {
            _uiState.value = ReturnsUiState.Loading
            repository.getReturns()
                .catch { e ->
                    _uiState.value = ReturnsUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { returns ->
                    _uiState.value = ReturnsUiState.Success(returns)
                }
        }
    }

    fun submitReturn(productId: String, customerId: String?, quantity: Int, reason: String) {
        viewModelScope.launch {
            val returnOrder = ReturnOrder(
                id = UUID.randomUUID().toString(),
                product_id = productId,
                customer_id = customerId,
                quantity = quantity,
                reason = reason
            )
            repository.createReturnOrder(returnOrder)
            loadReturns()
        }
    }

    fun updateReturnStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateReturnStatus(id, status)
            loadReturns()
        }
    }
}

class ReturnsViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReturnsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReturnsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
