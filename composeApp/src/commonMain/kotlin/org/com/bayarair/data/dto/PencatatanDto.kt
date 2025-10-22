package org.com.bayarair.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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
data class MeterRecordDto(
    val id: String,
    val meter: Int,
    val receipt: String,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    val customer: PelangganDto,
)

@Serializable
data class MeterRecord(
    val id: String,
    val meter: Int,
    val receipt: String,
    val status: String,
    val createdAt: Long,
    val customer: PelangganDto,
)

@OptIn(ExperimentalTime::class)
fun MeterRecordDto.toDomain(): MeterRecord =
    MeterRecord(
        id = id,
        meter = meter,
        receipt = receipt,
        status = status,
        createdAt = Instant.parse(createdAt).toEpochMilliseconds(),
        customer = customer,
    )

