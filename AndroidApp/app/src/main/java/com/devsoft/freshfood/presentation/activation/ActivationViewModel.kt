package com.devsoft.freshfood.presentation.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.freshfood.domain.repository.ActivationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ActivationState {
    object Checking : ActivationState()
    object Active : ActivationState()
    object Blocked : ActivationState()
    data class Error(val message: String) : ActivationState()
}

class ActivationViewModel(
    private val repository: ActivationRepository
) : ViewModel() {

    private val _activationState = MutableStateFlow<ActivationState>(ActivationState.Checking)
    val activationState: StateFlow<ActivationState> = _activationState.asStateFlow()

    init {
        checkActivation()
    }

    fun checkActivation() {
        viewModelScope.launch {
            _activationState.value = ActivationState.Checking
            val result = repository.isAppEnabled()
            result.fold(
                onSuccess = { enabled ->
                    if (enabled) {
                        _activationState.value = ActivationState.Active
                    } else {
                        _activationState.value = ActivationState.Blocked
                    }
                },
                onFailure = { error ->
                    _activationState.value = ActivationState.Error(
                        error.message ?: "Unable to verify application status. Please check your internet connection."
                    )
                }
            )
        }
    }
}

class ActivationViewModelFactory(
    private val repository: ActivationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
