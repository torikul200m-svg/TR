package com.example.trdial.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.trdial.data.local.UserProfileEntity
import com.example.trdial.data.local.UserSettings
import com.example.trdial.data.repository.AuthRepository
import com.example.trdial.data.repository.SettingsRepository
import com.example.trdial.security.InputValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorCode: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings?> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<UserProfileEntity?> = settingsRepository.settingsFlow
        .flatMapLatest { settings ->
            val id = settings.loggedInUserId
            if (id == null) flowOf(null) else authRepository.observeUser(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    fun clearError() {
        _authUiState.update { it.copy(errorCode = null) }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingCompleted(true)
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (!InputValidator.isValidEmail(email)) {
            _authUiState.update { it.copy(errorCode = "INVALID_EMAIL") }
            return
        }
        if (!InputValidator.isValidPassword(password)) {
            _authUiState.update { it.copy(errorCode = "SHORT_PASSWORD") }
            return
        }

        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, errorCode = null) }
            val result = authRepository.login(email, password)
            result.onSuccess { user ->
                settingsRepository.setLoggedInUserId(user.id)
                _authUiState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure {
                _authUiState.update { it.copy(isLoading = false, errorCode = "INVALID_CREDENTIALS") }
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        phoneExtension: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (fullName.isBlank()) {
            _authUiState.update { it.copy(errorCode = "REQUIRED_FIELD") }
            return
        }
        if (!InputValidator.isValidEmail(email)) {
            _authUiState.update { it.copy(errorCode = "INVALID_EMAIL") }
            return
        }
        if (!InputValidator.isValidPassword(password)) {
            _authUiState.update { it.copy(errorCode = "SHORT_PASSWORD") }
            return
        }
        if (password != confirmPassword) {
            _authUiState.update { it.copy(errorCode = "PASSWORD_MISMATCH") }
            return
        }

        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, errorCode = null) }
            val result = authRepository.register(fullName, email, phoneExtension, password)
            result.onSuccess { user ->
                settingsRepository.setOnboardingCompleted(true)
                settingsRepository.setLoggedInUserId(user.id)
                _authUiState.update { it.copy(isLoading = false) }
                onSuccess()
            }.onFailure { throwable ->
                val code = if (throwable.message == "EMAIL_EXISTS") "EMAIL_EXISTS" else "REGISTER_FAILED"
                _authUiState.update { it.copy(isLoading = false, errorCode = code) }
            }
        }
    }

    fun updateProfile(fullName: String, phoneExtension: String) {
        val user = currentUser.value ?: return
        if (fullName.isBlank()) return
        viewModelScope.launch {
            authRepository.updateDisplayName(user.id, fullName, phoneExtension)
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setLoggedInUserId(null)
            onComplete()
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val settingsRepository: SettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository, settingsRepository) as T
    }
}
