package org.com.bayarair.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

sealed interface SplashState {
    data object Loading : SplashState

    data object GoLogin : SplashState

    data object GoHome : SplashState
}

class SplashViewModel(
    private val tokenStore: TokenHandler,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        checkToken()
    }

    private fun checkToken() {
        viewModelScope.launch {
            val token = tokenStore.getToken()
            if (token.isNullOrBlank()) {
                _state.value = SplashState.GoLogin
                return@launch
            }
            _state.value = SplashState.GoHome
        }
    }
}
