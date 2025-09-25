package org.com.bayarair.data.model

import kotlinx.serialization.Serializable
import org.com.bayarair.data.dto.PelangganDto

data class Customer(
    val id: String,
    val name: String,
    val alamat: String,
    val hp: String,
    val meterLalu: Int,
)

fun PelangganDto.toDomain(): Customer =
    Customer(
        id = id,
        name = name,
        alamat = address,
        hp = phoneNumber,
        meterLalu = meterLalu,
    )

@Serializable
data class TotalCustomer(
    val total: Int,
)
