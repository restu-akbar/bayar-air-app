package org.com.bayarair.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PieChart(
    val bulan: Int,
    @SerialName("total_user") val totalUser: Int,
    @SerialName("total_user_lain") val totalUserLain: Int,
    val persentase: Double,
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
