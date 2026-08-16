package com.devsoft.freshfood.presentation.deliveries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.model.DeliveryOrderWithDetails
import com.devsoft.freshfood.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class DeliveryUiState {
    object Loading : DeliveryUiState()
    data class Success(val deliveries: List<DeliveryOrderWithDetails>) : DeliveryUiState()
    data class Error(val message: String) : DeliveryUiState()
}

class DeliveryViewModel(
    private val repository: DeliveryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeliveryUiState>(DeliveryUiState.Loading)
    val uiState: StateFlow<DeliveryUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        // loadDeliveries is called from LaunchedEffect in the UI
    }

    fun loadDeliveries() {
        viewModelScope.launch {
            _uiState.value = DeliveryUiState.Loading
            repository.getDeliveries()
                .catch { e ->
                    _uiState.value = DeliveryUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { deliveries ->
                    _uiState.value = DeliveryUiState.Success(deliveries)
                }
        }
    }

    fun updateDeliveryStatus(id: String, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateDeliveryStatus(id, newStatus)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteDeliveryOrder(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteDeliveryOrder(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateDeliveryItemsAndComplete(orderId: String, modifiedQuantities: Map<String, Int>) {
        viewModelScope.launch {
            try {
                repository.updateDeliveryItemsAndComplete(orderId, modifiedQuantities)
                loadDeliveries() // Refresh UI after completion
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.emit("Error updating delivery: ${e.message}")
            }
        }
    }
}

class DeliveryViewModelFactory(
    private val repository: DeliveryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
