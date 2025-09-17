package org.com.bayarair.core

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class AppEvent {
    object Logout : AppEvent()

    data class ShowSnackbar(
        val message: String,
    ) : AppEvent()
}

class AppEvents {
    private val _events =
        MutableSharedFlow<AppEvent>(
            replay = 1,
            extraBufferCapacity = 1,
        )
    val events = _events.asSharedFlow()

    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}
