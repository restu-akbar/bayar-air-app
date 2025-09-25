package org.com.bayarair.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AppEvent {
    data class Logout(
        val message: String? = null,
    ) : AppEvent()

    data class ShowSnackbar(
        val message: String,
    ) : AppEvent()
}

class AuthState {
    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut: StateFlow<Boolean> = _isLoggingOut

    fun beginLogout() {
        _isLoggingOut.value = true
    }

    fun endLogout() {
        _isLoggingOut.value = false
    }
}

class AppEvents {
    private val _events =
        MutableSharedFlow<AppEvent>(
            replay = 0,
            extraBufferCapacity = 64,
        )
    val events = _events.asSharedFlow()

    suspend fun emit(event: AppEvent) = _events.emit(event)

    fun tryEmit(event: AppEvent) {
        _events.tryEmit(event)
    }
}
