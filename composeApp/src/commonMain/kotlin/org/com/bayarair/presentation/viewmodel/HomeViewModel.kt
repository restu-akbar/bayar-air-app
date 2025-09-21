package org.com.bayarair.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.bayarair.data.model.MeterRecord
import org.com.bayarair.data.repository.MeterRecordRepository

class HomeViewModel(
    private val  repository: MeterRecordRepository,
) : ViewModel() {
    private val _records = MutableStateFlow<List<MeterRecord>>(emptyList())
    val records: StateFlow<List<MeterRecord>> = _records

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadHistory() {
        viewModelScope.launch {
            _loading.value = true
            repository.getRecords()
                .onSuccess { data ->
                    _records.value = data
                }
                .onFailure { e ->
                    _error.value = e.message
                }
            _loading.value = false
        }
    }

    fun getUserName() {
        viewModelScope.launch {
            _loading.value = true
            repository.getRecords()
                .onSuccess { data ->
                    _records.value = data
                }
                .onFailure { e ->
                    _error.value = e.message
                }
            _loading.value = false
        }
    }
}
