package org.com.bayarair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Pelanggan(
    val id: String,
    val name: String,
    val address: String,
    val phone_number: String,
    val rt: String,
    val rw: String,
    val created_at: String,
    val updated_at: String
)
