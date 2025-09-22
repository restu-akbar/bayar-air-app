package org.com.bayarair.data.dto

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import org.com.bayarair.data.model.MeterRecord


suspend fun HttpResponse.extractErrorMessage(preRead: String? = null): String {
    val txt = preRead ?: bodyAsText()
    if (txt.isBlank()) return "HTTP ${status.value} ${status.description}"
    val err = Serde.relaxed.decodeFromString<ErrorResponse>(txt)
    return try {
        err.errors?.values?.flatten()?.firstOrNull()
            ?: err.message
            ?: "HTTP ${status.value} ${status.description}"
    } catch (_: SerializationException) {
        txt.take(200)
    }
}

suspend inline fun <reified T> HttpResponse.unwrapFlexible(): BaseResponse<T> {
    val txt = bodyAsText()
    if (status.isSuccess()) {
        // Coba as envelope dulu
        try {
            val env = Serde.relaxed.decodeFromString<BaseResponse<T>>(txt)
            // Jika API menyatakan sukses tapi data null, anggap error semantik
            if (env.status && env.data == null) {
                throw ApiException(status.value, env.message.ifBlank { "Empty data" })
            }
            return env
        } catch (_: SerializationException) {
            // Bukan envelope → coba decode sebagai T polos, lalu bungkus
            val data = Serde.relaxed.decodeFromString<T>(txt)
            return BaseResponse(
                status = true,
                message = "",
                data = data
            )
        }
    } else {
        throw ApiException(status.value, extractErrorMessage(preRead = txt))
    }
}
