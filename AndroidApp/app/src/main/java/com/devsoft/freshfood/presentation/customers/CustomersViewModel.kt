package com.devsoft.freshfood.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class CustomersUiState {
    object Loading : CustomersUiState()
    data class Success(val customers: List<Customer>) : CustomersUiState()
    data class Error(val message: String) : CustomersUiState()
}

class CustomersViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomersUiState>(CustomersUiState.Loading)
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = CustomersUiState.Loading
            repository.getCustomers()
                .catch { e ->
                    _uiState.value = CustomersUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { customers ->
                    _uiState.value = CustomersUiState.Success(customers)
                }
        }
    }

    fun registerPayment(customerId: String, amount: Double) {
        viewModelScope.launch {
            try {
                val payment = com.devsoft.freshfood.domain.model.Payment(
                    customer_id = customerId,
                    amount = amount,
                    payment_method = "CASH",
                    user_id = "00000000-0000-0000-0000-000000000000" // MOCK USER
                )
                repository.registerPayment(payment)
                // Reload customers to get updated credit
                loadCustomers()
            } catch (e: Exception) {
                // In a real app we'd show a toast or error state here
            }
        }
    }

    fun addCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                repository.insertCustomer(customer)
                loadCustomers()
            } catch (e: Exception) {
                _uiState.value = CustomersUiState.Error(e.message ?: "Failed to add customer")
            }
        }
    }
}

class CustomersViewModelFactory(
    private val repository: CustomerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
