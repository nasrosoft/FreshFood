package com.devsoft.devsoft.presentation.activation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.devsoft.domain.repository.ActivationRepository
import com.devsoft.devsoft.utils.NetworkHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ActivationState {
    object Checking : ActivationState()
    object Active : ActivationState()
    object Blocked : ActivationState()
    data class NoInternet(val message: String? = null) : ActivationState()
}

class ActivationViewModel(
    private val repository: ActivationRepository,
    private val context: Context
) : ViewModel() {

    private val _activationState = MutableStateFlow<ActivationState>(ActivationState.Checking)
    val activationState: StateFlow<ActivationState> = _activationState.asStateFlow()

    init {
        checkActivation()
    }

    fun checkActivation() {
        viewModelScope.launch {
            _activationState.value = ActivationState.Checking

            // 1. Check local network connectivity first
            if (!NetworkHelper.isOnline(context)) {
                _activationState.value = ActivationState.NoInternet(
                    "No internet connection detected. Please check your connection."
                )
                return@launch
            }

            // 2. Perform check with retry for slow network
            var attempts = 0
            var success = false

            while (attempts < 2 && !success) {
                attempts++
                val result = repository.isAppEnabled()
                result.fold(
                    onSuccess = { enabled ->
                        success = true
                        if (enabled) {
                            _activationState.value = ActivationState.Active
                        } else {
                            _activationState.value = ActivationState.Blocked
                        }
                    },
                    onFailure = { error ->
                        if (attempts >= 2) {
                            _activationState.value = ActivationState.NoInternet(
                                "Unable to reach server. Please check your internet connection."
                            )
                        } else {
                            delay(1000) // brief delay before 1 automatic retry
                        }
                    }
                )
            }
        }
    }
}

class ActivationViewModelFactory(
    private val repository: ActivationRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivationViewModel(repository, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
