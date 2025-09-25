package org.com.bayarair.data.model

import kotlinx.serialization.Serializable
import org.com.bayarair.data.dto.PelangganDto

@Serializable
data class User(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val phone_number: String,
)
