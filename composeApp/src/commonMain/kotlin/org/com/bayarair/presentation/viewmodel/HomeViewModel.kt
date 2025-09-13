package org.com.bayarair.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

class HomeViewModel(
    private val tokenHandler: TokenHandler,
    private val authRepository: AuthRepository,
) : ViewModel() {
    fun logout(
        onFinally: () -> Unit,
        onError: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            val token = tokenHandler.getToken()
            val result =
                if (token != null) {
                    authRepository.logout(token)
                } else {
                    Result.success(Unit)
                }

            result.onFailure { e ->
                onError(e.message ?: "Gagal logout")
            }

            tokenHandler.clear()
            onFinally()
        }
    }
}
