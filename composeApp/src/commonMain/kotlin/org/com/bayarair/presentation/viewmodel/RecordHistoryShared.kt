package org.com.bayarair.presentation.viewmodel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.com.bayarair.data.dto.MeterRecord

class RecordHistoryShared {
    private val _history = MutableStateFlow<List<MeterRecord>>(emptyList())
    val history: StateFlow<List<MeterRecord>> = _history.asStateFlow()

    fun prepend(h: MeterRecord) {
        _history.update { current ->
            if (current.isNotEmpty()) listOf(h) + current else current
        }
    }

    fun setHistory(list: List<MeterRecord>) {
        _history.value = list
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
