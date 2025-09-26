package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

sealed interface AuthState {
    data object Idle : AuthState

    data object Loading : AuthState

    data class Success(
        val message: String,
    ) : AuthState

    data class ShowSnackbar(
        val message: String,
    ) : AuthState
}

class AuthViewModel(
    private val tokenHandler: TokenHandler,
    private val authRepository: AuthRepository,
    private val appEvents: AppEvents,
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

            authRepository
                .login(l, p)
                .onSuccess { env ->
                    val t = env.data!!.token
                    tokenHandler.setToken(t)
                    _state.value = AuthState.Success(env.message)
                }.onFailure { e ->
                    _password.value = ""
                    _state.value = AuthState.ShowSnackbar(e.message ?: "Login gagal")
                }
        }
    }

    fun logout() {
        screenModelScope.launch {
            val result = authRepository.logout()
            result
                .onSuccess { env ->
                    _state.value = AuthState.Idle
                    appEvents.emit(AppEvent.Logout(env.message))
                }.onFailure { e ->
                    appEvents.emit(AppEvent.Logout(e.message ?: " Logout gagal"))
                }
            runCatching { tokenHandler.clear() }
        }
    }
}
