package org.com.bayarair.data.model

import kotlinx.serialization.Serializable
import org.com.bayarair.data.dto.PelangganDto

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val address: String,
    val hp: String,
    val meterLalu: Int,
)

fun PelangganDto.toDomain(): Customer =
    Customer(
        id = id,
        name = name,
        address = address,
        hp = phoneNumber,
        meterLalu = meterLalu,
    )

@Serializable
data class TotalCustomer(
    val total: Int,
)
