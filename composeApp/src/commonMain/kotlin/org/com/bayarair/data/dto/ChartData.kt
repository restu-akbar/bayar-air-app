package org.com.bayarair.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class PieChart(
    val bulan: Int,
    val total: Int,
    val persentase: Int,
)

@Serializable
data class BarChart(
    val tahun: Int,
    val data: List<MonthlyData>,
)

@Serializable
data class MonthlyData(
    val bulan: String,
    val total: Int,
)
