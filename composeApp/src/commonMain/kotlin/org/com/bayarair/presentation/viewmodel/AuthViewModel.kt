package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

sealed interface AuthState {
    data object Idle : AuthState

    data object Loading : AuthState

    data class Error(
        val message: String,
    ) : AuthState

    data object Success : AuthState
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

    fun onLoginChange(v: String) {
        _login.value = v
    }

    fun onPasswordChange(v: String) {
        _password.value = v
    }

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
                }.onFailure { e ->
                    _password.value = ""
                    _state.value = AuthState.Error(e.message ?: "Login gagal")
                }
        }
    }
}
