package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

class ProfileViewModel(
    private val tokenHandler: TokenHandler,
    private val authRepo: AuthRepository,
) : ScreenModel {
    private val _onErrorMessage = MutableSharedFlow<String>()
    val onErrorMessage: SharedFlow<String> = _onErrorMessage

    private val _onLoggedOut = MutableSharedFlow<Boolean>()
    val onLoggedOut: SharedFlow<Boolean> = _onLoggedOut

    fun logout() {
        screenModelScope.launch {
            val token = tokenHandler.getToken()
            val result =
                if (token.isNullOrBlank()) {
                    Result.failure(IllegalStateException("Token kosong"))
                } else {
                    runCatching { authRepo.logout(token) }
                }

            runCatching { tokenHandler.clear() }

            result.onFailure { e ->
                _onErrorMessage.emit(e.message ?: "Logout gagal")
            }
            _onLoggedOut.emit(true)
        }
    }
}
