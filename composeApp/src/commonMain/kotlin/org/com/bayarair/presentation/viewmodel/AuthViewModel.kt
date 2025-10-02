package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler

data class AuthState(
    val login: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val success: Boolean = false,
)

class AuthViewModel(
    private val tokenHandler: TokenHandler,
    private val authRepository: AuthRepository,
    private val appEvents: AppEvents,
    private val historyShared: RecordHistoryShared,
    private val userShared: UserShared,
    private val statsShared: StatsShared,
) : StateScreenModel<AuthState>(AuthState()) {
    fun onLoginChange(v: String) {
        mutableState.update { it.copy(login = v) }
    }

    fun onPasswordChange(v: String) {
        mutableState.update { it.copy(password = v) }
    }

    fun onLoginClick() {
        val s = state.value
        if (s.isLoading) return

        mutableState.update { it.copy(isLoading = true) }

        screenModelScope.launch {
            val l = state.value.login.trim()
            val p = state.value.password

            authRepository
                .login(l, p)
                .onSuccess { env ->
                    val t = requireNotNull(env.data).token
                    tokenHandler.setToken(t)
                    appEvents.emit(AppEvent.ShowSnackbar(env.message))
                    mutableState.update { it.copy(isLoading = false, success = true) }
                }.onFailure { e ->
                    appEvents.emit(AppEvent.ShowSnackbar(e.message ?: "Login gagal"))
                    mutableState.update {
                        it.copy(password = "", isLoading = false, success = false)
                    }
                }
        }
    }

    fun consumeSuccess() {
        if (state.value.success) {
            mutableState.update { it.copy(success = false) }
        }
    }

    fun logout() {
        screenModelScope.launch {
            historyShared.clearHistory()
            statsShared.clearBarChart()
            statsShared.clearPieChart()
            userShared.clearUser()

            val result = authRepository.logout()
            result
                .onSuccess { env -> appEvents.emit(AppEvent.Logout(env.message)) }
                .onFailure { e -> appEvents.emit(AppEvent.Logout(e.message ?: " Logout gagal")) }

            runCatching { tokenHandler.clear() }
            mutableState.update { AuthState() }
        }
    }
}
