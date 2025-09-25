package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.dto.isUnauthorized
import org.com.bayarair.data.model.User
import org.com.bayarair.data.repository.ProfileRepository

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val appEvents: AppEvents,
) : StateScreenModel<ProfileState>(ProfileState()) {
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading

    fun getUser(force: Boolean = false) {
        if (!force && state.value.user != null) return
        screenModelScope.launch {
            _loading.value = true
            try {
                val result = repository.getUser()
                result
                    .onSuccess { user ->
                        mutableState.update { it.copy(user = user) }
                    }.onFailure { e ->
                        if (!e.isUnauthorized()) {
                            appEvents.emit(
                                AppEvent.ShowSnackbar(
                                    e.message ?: "Terjadi kesalahan",
                                ),
                            )
                        }
                    }
            } finally {
                _loading.value = false
            }
        }
    }
}

data class ProfileState(
    val user: User? = null,
)
