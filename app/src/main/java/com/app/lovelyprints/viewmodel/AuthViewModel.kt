package com.app.lovelyprints.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lovelyprints.core.auth.TokenManager
import com.app.lovelyprints.data.repository.AuthRepository
import com.app.lovelyprints.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val tokenManager: TokenManager   // can stay for logout later
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        Log.d("AUTH", "VM LOGIN CALLED")
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            when (val result = authRepository.login(email, password)) {

                is Result.Success -> {
                    // ✅ Repository already saved token
                    _uiState.value = AuthUiState(isSuccess = true)
                }

                is Result.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }

                else -> Unit
            }
        }
    }

    fun signup(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)

            when (val result = authRepository.signup(name, email, password)) {
                is Result.Success -> {
                    _uiState.value = AuthUiState(isSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = AuthUiState(error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

