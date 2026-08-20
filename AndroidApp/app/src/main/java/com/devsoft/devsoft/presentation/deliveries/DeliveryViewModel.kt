package com.devsoft.devsoft.presentation.deliveries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.devsoft.domain.model.DeliveryOrderWithDetails
import com.devsoft.devsoft.domain.repository.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
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
        loadDeliveries() // Called immediately so notification intents can access loaded data
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
                loadDeliveries() // Refresh UI after completion
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.emit("Error updating status: ${e.message}")
            }
        }
    }

    fun deleteDeliveryOrder(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteDeliveryOrder(id)
                loadDeliveries() // Refresh UI after completion
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.emit("Error deleting delivery: ${e.message}")
            }
        }
    }

    fun updateDeliveryItemsAndComplete(orderId: String, modifiedQuantities: Map<String, Int>, finalPaymentMethod: String? = null) {
        viewModelScope.launch {
            try {
                repository.updateDeliveryItemsAndComplete(orderId, modifiedQuantities, finalPaymentMethod)
                loadDeliveries() // Refresh UI after completion
            } catch (e: Exception) {
                e.printStackTrace()
                _errorMessage.emit("Error updating delivery: ${e.message}")
            }
        }
    }

    fun checkAndNotifyNewDeliveries(context: android.content.Context, currentUserId: String?) {
        viewModelScope.launch {
            try {
                repository.getDeliveries().collect { deliveries ->
                    // Update state if we are currently success or loading
                    if (_uiState.value !is DeliveryUiState.Loading) {
                        _uiState.value = DeliveryUiState.Success(deliveries)
                    }
                    
                    deliveries.forEach { item ->
                        val isAssignedToUser = item.order.delivery_employee_id == currentUserId || currentUserId == null
                        val isPendingOrAssigned = item.order.status.equals("PENDING", ignoreCase = true) ||
                                item.order.status.equals("ASSIGNED", ignoreCase = true)

                        if (isAssignedToUser && isPendingOrAssigned) {
                            val orderId = item.order.id
                            if (!com.devsoft.devsoft.utils.NotificationHelper.isOrderNotified(context, orderId)) {
                                val customerName = item.customer?.name ?: "Client"
                                val shortId = orderId.take(8).uppercase()
                                com.devsoft.devsoft.utils.NotificationHelper.showDeliveryNotification(
                                    context = context,
                                    title = "Nouvelle livraison assignée 🚚",
                                    message = "Commande #$shortId pour $customerName prête pour la livraison.",
                                    orderId = orderId
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore transient network errors during background poll
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
