package org.com.bayarair.data.dto

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException

suspend fun HttpResponse.extractErrorMessage(preRead: String? = null): String {
    val txt = preRead ?: bodyAsText()
    if (txt.isBlank()) return "HTTP ${status.value} ${status.description}"
    return try {
        val err = Serde.relaxed.decodeFromString<ErrorResponse>(txt)
        err.errors
            ?.values
            ?.flatten()
            ?.firstOrNull()
            ?: err.message
            ?: "HTTP ${status.value} ${status.description}"
    } catch (_: SerializationException) {
        txt.take(200)
    }
}

suspend inline fun <reified T> HttpResponse.unwrapFlexible(): BaseResponse<T> {
    val txt = bodyAsText()
    if (status.isSuccess()) {
        try {
            val env = Serde.relaxed.decodeFromString<BaseResponse<T>>(txt)
            if (env.status && env.data == null) {
                throw ApiException(status.value, env.message.ifBlank { "Empty data" })
            }
            return env
        } catch (_: SerializationException) {
            val data = Serde.relaxed.decodeFromString<T>(txt)
            return BaseResponse(
                status = true,
                message = "",
                data = data,
            )
        }
    } else {
        throw ApiException(status.value, extractErrorMessage(preRead = txt))
    }
}

fun Throwable.isUnauthorized(): Boolean = (this as? ApiException)?.code == 401
