package org.com.bayarair.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val login: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
)
