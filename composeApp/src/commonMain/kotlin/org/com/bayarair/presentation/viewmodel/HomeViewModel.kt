package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.dto.BarChart
import org.com.bayarair.data.dto.PieChart
import org.com.bayarair.data.dto.isUnauthorized
import org.com.bayarair.data.model.MeterRecord
import org.com.bayarair.data.repository.CustomerRepository
import org.com.bayarair.data.repository.RecordRepository

class HomeViewModel(
    private val recordRepo: RecordRepository,
    private val custRepo: CustomerRepository,
    private val appEvents: AppEvents,
) : StateScreenModel<HomeState>(HomeState()) {
    fun init(
        force: Boolean,
        month: Int,
        isPieChart: Boolean,
        year: Int = 2025,
    ) {
        if (!force) return
        mutableState.update { it.copy(loading = true) }
        getTotalCustomers(force)
        if (isPieChart) {
            getPieChartData(force, month)
        } else {
            getBarChartData(force, year)
        }
    }

    fun getTotalCustomers(force: Boolean = false) {
        if (!force) return
        screenModelScope.launch {
            mutableState.update { it.copy(loading = true) }
            custRepo
                .getTotalPelanggan()
                .onSuccess { data ->
                    mutableState.update { it.copy(totalCust = data) }
                }.onFailure { e ->
                    if (!e.isUnauthorized()) {
                        appEvents.emit(AppEvent.ShowSnackbar(e.message ?: "Terjadi kesalahan"))
                    }
                }
            mutableState.update { it.copy(loading = false) }
        }
    }

    fun getPieChartData(
        force: Boolean = false,
        month: Int,
    ) {
        if (!force && state.value.pieChart != null) return
        screenModelScope.launch {
            mutableState.update { it.copy(loading = true) }
            recordRepo
                .getMonthlyStats(month)
                .onSuccess { data ->
                    mutableState.update { it.copy(pieChart = data) }
                }.onFailure { e ->
                    if (!e.isUnauthorized()) {
                        appEvents.emit(AppEvent.ShowSnackbar(e.message ?: "Terjadi kesalahan"))
                    }
                }
            mutableState.update { it.copy(loading = false) }
        }
    }

    fun getBarChartData(
        force: Boolean = false,
        year: Int,
    ) {
        if (!force && state.value.pieChart != null) return
        screenModelScope.launch {
            mutableState.update { it.copy(loading = true) }
            recordRepo
                .getYearlyStats(year)
                .onSuccess { data ->
                    mutableState.update {
                        it.copy(barChart = data)
                    }
                }.onFailure { e ->
                    if (!e.isUnauthorized()) {
                        appEvents.emit(AppEvent.ShowSnackbar(e.message ?: "Terjadi kesalahan"))
                    }
                }
            mutableState.update { it.copy(loading = false) }
        }
    }

    fun loadHistory(force: Boolean = false) {
        if (!force && state.value.history.isNotEmpty()) return
        screenModelScope.launch {
            mutableState.update { it.copy(loading = true) }
            recordRepo
                .getRecords()
                .onSuccess { data ->
                    mutableState.update { it.copy(history = data) }
                }.onFailure { e ->
                    if (!e.isUnauthorized()) {
                        appEvents.emit(AppEvent.ShowSnackbar(e.message ?: "Terjadi kesalahan"))
                    }
                }
            mutableState.update { it.copy(loading = false) }
        }
    }
}

data class HomeState(
    val pieChart: PieChart? = null,
    val barChart: BarChart? = null,
    val history: List<MeterRecord?> = emptyList(),
    val loading: Boolean = false,
    val totalCust: Int = 0,
)
