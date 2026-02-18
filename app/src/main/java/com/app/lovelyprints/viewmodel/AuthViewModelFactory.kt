package com.app.lovelyprints.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.lovelyprints.di.AppContainer

class AuthViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(
                appContainer.authRepository,
                appContainer.tokenManager,
                appContainer.notificationApi
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}