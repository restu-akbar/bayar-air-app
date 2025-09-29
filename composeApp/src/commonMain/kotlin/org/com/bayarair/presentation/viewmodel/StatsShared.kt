package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.com.bayarair.data.dto.BarChart
import org.com.bayarair.data.dto.PieChart

class StatsShared {
    private val _pieChart = MutableStateFlow<PieChart?>(null)
    val pieChart: StateFlow<PieChart?> = _pieChart.asStateFlow()

    private val _barChart = MutableStateFlow<BarChart?>(null)
    val barChart: StateFlow<BarChart?> = _barChart.asStateFlow()

    fun setPieChart(pieChart: PieChart) {
        _pieChart.value = pieChart
    }

    fun clearPieChart() {
        _pieChart.value = null
    }

    fun setBarChart(barChart: BarChart) {
        _barChart.value = barChart
    }

    fun clearBarChart() {
        _barChart.value = null
    }
}
