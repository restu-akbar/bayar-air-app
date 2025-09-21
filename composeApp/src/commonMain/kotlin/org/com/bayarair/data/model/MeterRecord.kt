package org.com.bayarair.data.model

import kotlinx.serialization.Serializable
import org.com.bayarair.data.dto.PelangganDto

@Serializable
data class MeterRecord(
    val id: String,
    val customer_id: String,
    val user_id: String,
    val meter: Int,
    val evidence: String,
    val receipt: String,
    val total_amount: Long,
    val fine: Long,
    val duty_stamp: Long,
    val retribution_fee: Long,
    val status: String,
    val created_at: String,
    val updated_at: String,
)


