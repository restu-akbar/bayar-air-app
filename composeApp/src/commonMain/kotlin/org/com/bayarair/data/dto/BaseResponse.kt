package org.com.bayarair.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T? = null,
)

@Serializable
data class ErrorResponse(
    val message: String? = null,
    val errors: Map<String, List<String>>? = null,
)

class ApiException(
    val code: Int,
    override val message: String,
) : Exception(message)
