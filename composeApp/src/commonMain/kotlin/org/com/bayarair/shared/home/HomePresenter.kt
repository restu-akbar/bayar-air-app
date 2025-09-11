package org.com.bayarair.shared.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.data.repository.GreetingRepo

class HomePresenter(
    private val repo: GreetingRepo,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(HomeState(loading = true))
    val state: StateFlow<HomeState> = _state

    fun load() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repo.greet() }
                .onSuccess { msg -> _state.update { it.copy(loading = false, greeting = msg) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun clear() {
    }
}
