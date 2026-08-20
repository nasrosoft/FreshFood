package com.devsoft.devsoft.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.devsoft.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class AuthState {
    object Checking : AuthState()
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val role: String, val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val profileRepository: com.devsoft.devsoft.domain.repository.ProfileRepository
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Checking)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            _authState.value = AuthState.Checking
            val result = repository.restoreSession()
            val userId = result.getOrNull()
            if (userId != null) {
                try {
                    val profile = profileRepository.getProfileById(userId)
                    _authState.value = AuthState.Authenticated(profile?.role ?: "SELLER", userId)
                } catch (e: Exception) {
                    // Fallback to authenticated state even if offline
                    _authState.value = AuthState.Authenticated("SELLER", userId)
                }
            } else {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.login(email, password).fold(
                onSuccess = { 
                    checkAuthStatus()
                },
                onFailure = { _authState.value = AuthState.Error(it.message ?: "Login failed") }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _authState.value = AuthState.Idle
        }
    }

    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Idle
        }
    }
}

class AuthViewModelFactory(
    private val repository: AuthRepository,
    private val profileRepository: com.devsoft.devsoft.domain.repository.ProfileRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthViewModel(repository, profileRepository) as T
    }
}
