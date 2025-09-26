package org.com.bayarair.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProfileForm(
    val name: String = "",
    val username: String = "",
    val phone_number: String = "",
    val email: String = "",
)
