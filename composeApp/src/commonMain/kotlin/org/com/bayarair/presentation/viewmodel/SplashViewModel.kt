package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.bayarair.data.token.TokenHandler

sealed interface SplashState {
    data object Loading : SplashState

    data object GoLogin : SplashState

    data object GoHome : SplashState
}

class SplashViewModel(
    private val tokenStore: TokenHandler,
) : ScreenModel {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        screenModelScope.launch {
            val token = tokenStore.getToken()
            _state.value = if (token.isNullOrBlank()) SplashState.GoLogin else SplashState.GoHome
        }
    }
}
