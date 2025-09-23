package org.com.bayarair.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PelangganDto(
    val id: String,
    val name: String,
    val address: String,
    @SerialName("phone_number") val phoneNumber: String,
    val rt: String? = null,
    val rw: String? = null,
    @SerialName("meter_lalu") val meterLalu: Int = 0,
)

@Serializable
data class HargaData(
    @SerialName("air") val air: Long,
    @SerialName("admin") val admin: Long,
)

@Serializable
data class SaveRecordResponse(
    val pencatatan: Pencatatan,
    val struk: StrukDto,
)

@Serializable
data class Pencatatan(
    val id: String,
)

@Serializable
data class StrukDto(
    val url: String,
    val filename: String,
)
