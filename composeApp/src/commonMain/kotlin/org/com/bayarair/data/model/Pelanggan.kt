package org.com.bayarair.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Pelanggan(
    val id: String,
    val name: String,
    val address: String,
)
