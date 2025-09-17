package org.com.bayarair.data.dto

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.decodeFromString


suspend fun HttpResponse.extractErrorMessage(preRead: String? = null): String {
    val txt = preRead ?: bodyAsText()
    if (txt.isBlank()) return "HTTP ${status.value} ${status.description}"
    return try {
        val err = Serde.relaxed.decodeFromString<ErrorResponse>(txt)
        err.errors?.values?.flatten()?.firstOrNull()
            ?: err.message
            ?: "HTTP ${status.value} ${status.description}"
    } catch (_: SerializationException) {
        txt.take(200)
    }
}

suspend inline fun <reified T> HttpResponse.unwrapFlexible(): T {
    val txt = bodyAsText()
    return if (status.isSuccess()) {
        try {
            val env = Serde.relaxed.decodeFromString<BaseResponse<T>>(txt)
            env.data ?: throw ApiException(status.value, env.message.ifBlank { "Empty data" })
        } catch (_: SerializationException) {
            Serde.relaxed.decodeFromString<T>(txt)
        }
    } else {
        throw ApiException(status.value, extractErrorMessage(preRead = txt))
    }
}
