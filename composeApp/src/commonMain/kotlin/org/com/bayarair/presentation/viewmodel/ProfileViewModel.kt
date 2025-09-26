package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.dto.ProfileForm
import org.com.bayarair.data.dto.isUnauthorized
import org.com.bayarair.data.model.User
import org.com.bayarair.data.repository.ProfileRepository

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val appEvents: AppEvents,
) : StateScreenModel<ProfileState>(ProfileState()) {
    fun prefill(user: User) {
        mutableState.update {
            it.copy(
                user = user,
                loading = false,
                profileForm =
                    ProfileForm(
                        name = user.name,
                        username = user.username,
                        phone_number = user.phone_number,
                        email = user.email,
                    ),
            )
        }
        revalidate()
    }

    fun getUser(force: Boolean = false) {
        if (!force && state.value.user != null) return
        screenModelScope.launch {
            mutableState.update { it.copy(loading = true) }
            try {
                val result = repository.getUser()
                result
                    .onSuccess { user ->
                        mutableState.update {
                            it.copy(
                                user = user,
                                profileForm =
                                    ProfileForm(
                                        name = user.name,
                                        username = user.username,
                                        phone_number = user.phone_number,
                                        email = user.email,
                                    ),
                            )
                        }
                        revalidate()
                    }.onFailure { e ->
                        if (!e.isUnauthorized()) {
                            appEvents.emit(AppEvent.ShowSnackbar(e.message ?: "Terjadi kesalahan"))
                        }
                    }
            } finally {
                mutableState.update { it.copy(loading = false) }
            }
        }
    }

    fun onNamaChange(v: String) {
        setForm(state.value.profileForm.copy(name = v))
    }

    fun onUsernameChange(v: String) {
        setForm(state.value.profileForm.copy(username = v.lowercase().replace(" ", "")))
    }

    fun onNoHpChange(v: String) {
        val filtered = v.filterIndexed { idx, c -> c.isDigit() || (c == '+' && idx == 0) }
        setForm(state.value.profileForm.copy(phone_number = filtered))
    }

    fun onEmailChange(v: String) {
        setForm(state.value.profileForm.copy(email = v.trim()))
    }

    private fun setForm(form: ProfileForm) {
        mutableState.update { it.copy(profileForm = form) }
        revalidate()
    }

    private fun revalidate() {
        val f = state.value.profileForm
        val err =
            ProfileFormErrors(
                name = if (f.name.trim().length < 3) "Nama minimal 3 karakter" else null,
                username =
                    if (!Regex("^[a-z0-9._]{5,10}\$").matches(f.username)) {
                        "Username 5–10 huruf kecil/angka/._ tanpa spasi"
                    } else {
                        null
                    },
                phone_number =
                    if (!Regex("^\\+?[0-9]{9,15}\$").matches(f.phone_number)) {
                        "No HP minimal 10 - 13 digit"
                    } else {
                        null
                    },
                email =
                    if (!Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
                            .matches(f.email)
                    ) {
                        "Format email tidak valid"
                    } else {
                        null
                    },
            )
        val nonEmpty =
            f.name.isNotBlank() && f.username.isNotBlank() && f.phone_number.isNotBlank() && f.email.isNotBlank()
        val canSubmit =
            listOf(
                err.name,
                err.username,
                err.phone_number,
                err.email,
            ).all { it == null } && nonEmpty

        mutableState.update { it.copy(errors = err, canSubmit = canSubmit) }
    }

    fun updateProfile() {
        val s = state.value
        if (!s.canSubmit || s.saving) return

        screenModelScope.launch {
            mutableState.update { it.copy(saving = true) }
            try {
                val form = s.profileForm
                val result =
                    repository.updateProfile(
                        form,
                        s.user!!.id,
                    )
                result
                    .onSuccess { updatedUser ->
                        mutableState.update {
                            it.copy(user = updatedUser)
                        }
                        appEvents.emit(AppEvent.ShowSnackbar("Profil berhasil diperbarui"))
                    }.onFailure { e ->
                        appEvents.emit(
                            AppEvent.ShowSnackbar(
                                e.message ?: "Gagal memperbarui profil",
                            ),
                        )
                    }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }
}


data class ProfileFormErrors(
    val name: String? = null,
    val username: String? = null,
    val phone_number: String? = null,
    val email: String? = null,
)

data class ProfileState(
    val user: User? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val profileForm: ProfileForm = ProfileForm(),
    val errors: ProfileFormErrors = ProfileFormErrors(),
    val canSubmit: Boolean = false,
)
