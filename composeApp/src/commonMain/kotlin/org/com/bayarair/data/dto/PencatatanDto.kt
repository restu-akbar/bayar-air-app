
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
    val harga: Long,
)
