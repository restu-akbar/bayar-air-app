package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Error(val message: String) : AuthState
    data object Success : AuthState
}

sealed interface AuthEvent {
    data object LoggedOut : AuthEvent
    data class LogoutError(val message: String) : AuthEvent
}

class AuthViewModel(
    private val tokenStore: TokenHandler,
    private val authRepository: AuthRepository,
) : ScreenModel {

    private val _login = MutableStateFlow("")
    val login: StateFlow<String> = _login

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events

    fun onLoginChange(v: String) { _login.value = v }
    fun onPasswordChange(v: String) { _password.value = v }

    fun onLoginClick() {
        if (_state.value == AuthState.Loading) return
        _state.value = AuthState.Loading

        screenModelScope.launch {
            val l = _login.value.trim()
            val p = _password.value

            val result = authRepository.login(l, p)
            result
                .onSuccess { token ->
                    tokenStore.setToken(token)
                    _state.value = AuthState.Success
                }
                .onFailure { e ->
                    _password.value = ""
                    _state.value = AuthState.Error(e.message ?: "Login gagal")
                }
        }
    }

    fun logout() {
        screenModelScope.launch {
            val token = tokenStore.getToken()
            val result = if (token.isNullOrBlank()) {
                Result.failure(IllegalStateException("Token kosong"))
            } else {
                runCatching { authRepository.logout(token) }
            }

            runCatching { tokenStore.clear() }

            result.onFailure { e ->
                _events.emit(AuthEvent.LogoutError(e.message ?: "Logout gagal"))
            }
            _events.emit(AuthEvent.LoggedOut)
        }
    }
}
