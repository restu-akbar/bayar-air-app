package org.com.bayarair.presentation.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

sealed interface LoginState {
    data object Idle : LoginState

    data object Loading : LoginState

    data class Error(
        val message: String,
    ) : LoginState

    data object Success : LoginState
}

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenHandler,
) : ViewModel() {
    private val _login = MutableStateFlow("")
    val login: StateFlow<String> = _login

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state

    fun onLoginChange(v: String) {
        _login.value = v
    }

    fun onPasswordChange(v: String) {
        _password.value = v
    }

    fun onLoginClick() {
        if (_state.value == LoginState.Loading) return
        _state.value = LoginState.Loading

        viewModelScope.launch {
            val u = _login.value.trim()
            val p = _password.value

            val result = authRepository.login(u, p)
            result
                .onSuccess { token ->
                    tokenStore.setToken(token)
                    _state.value = LoginState.Success
                }.onFailure { e ->
                    _state.value = LoginState.Error(e.message ?: "Login gagal")
                }
        }
    }
}
