package org.com.bayarair.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AppEvent {
    data class Logout(
        val message: String? = null,
    ) : AppEvent()

    data class ShowSnackbar(
        val message: String,
    ) : AppEvent()
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
