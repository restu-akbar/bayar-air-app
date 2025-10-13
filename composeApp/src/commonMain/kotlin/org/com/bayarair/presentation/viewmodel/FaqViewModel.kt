package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.dto.Faq
import org.com.bayarair.data.dto.isUnauthorized
import org.com.bayarair.data.repository.FaqRepository

class FaqViewModel(
    private val repository: FaqRepository,
    private val appEvents: AppEvents
) : StateScreenModel<FaqState>(FaqState()) {
    fun getFaq(force: Boolean = false) {
        val cached = state.value.faqs
        if (!force && cached.isNotEmpty()) {
            return
        }
        screenModelScope.launch {
            mutableState.update { it.copy(loading = true) }
            try {
                val result = repository.getFaq()
                result
                    .onSuccess { data ->
                        mutableState.update {
                            it.copy(
                                faqs = data,
                            )
                        }
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
}

data class FaqState(
    val faqs: List<Faq?> = emptyList(),
    val loading: Boolean = false
)
