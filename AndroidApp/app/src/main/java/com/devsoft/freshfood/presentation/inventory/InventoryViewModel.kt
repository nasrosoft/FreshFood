package com.devsoft.freshfood.presentation.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.model.InventoryItem
import com.devsoft.freshfood.domain.model.InventorySession
import com.devsoft.freshfood.domain.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.UUID

sealed class InventoryUiState {
    object Loading : InventoryUiState()
    data class Success(val sessions: List<InventorySession>) : InventoryUiState()
    data class Error(val message: String) : InventoryUiState()
}

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InventoryUiState>(InventoryUiState.Loading)
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.value = InventoryUiState.Loading
            repository.getInventorySessions()
                .catch { e ->
                    _uiState.value = InventoryUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { sessions ->
                    _uiState.value = InventoryUiState.Success(sessions)
                }
        }
    }

    fun startNewSession(notes: String, items: List<InventoryItem>) {
        viewModelScope.launch {
            val session = InventorySession(
                id = UUID.randomUUID().toString(),
                notes = notes
            )
            repository.createInventorySession(session, items)
            loadSessions()
        }
    }
}

class InventoryViewModelFactory(
    private val repository: InventoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
