package org.com.bayarair.platform

import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.com.bayarair.data.dto.ApiException
import org.com.bayarair.data.dto.asApiException
import org.com.bayarair.data.dto.extractErrorMessage

typealias FastTokenProvider = () -> String?
typealias SessionVersionProvider = () -> Int

private const val HEADER_SESSION_VER = "X-Session-Version"

fun installCommonPlugins(
    client: HttpClient,
    fastTokenProvider: FastTokenProvider = { null },
    sessionVersionProvider: SessionVersionProvider = { 0 },
    shouldAttach: (String) -> Boolean = { path -> !path.contains("/login") },
    onUnauthorized: suspend () -> Unit = {},
) = client.config {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                coerceInputValues = true
            },
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000

        connectTimeoutMillis = 10_000

        socketTimeoutMillis = 30_000
    }

    install(DefaultRequest) {
        contentType(ContentType.Application.Json)
        accept(ContentType.Application.Json)

        val path = "/" + url.pathSegments.joinToString("/").lowercase()
        if (shouldAttach(path)) {
            fastTokenProvider()?.let { token ->
                headers.append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        headers.append(HEADER_SESSION_VER, sessionVersionProvider().toString())
    }

    HttpResponseValidator {
        validateResponse { response ->
            if (!response.status.isSuccess()) {
                val msg = response.extractErrorMessage()
                throw ApiException(response.status.value, msg) as Throwable
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                val path =
                    "/" +
                            response.call.request.url.rawSegments
                                .joinToString("/")
                                .lowercase()
                if (!shouldAttach(path)) return@validateResponse

                val reqSnapshot =
                    response.call.request.headers[HEADER_SESSION_VER]
                        ?.toIntOrNull()
                val nowVersion = sessionVersionProvider()

                if (reqSnapshot != null && reqSnapshot == nowVersion) {
                    onUnauthorized()
                }
            }
        }
        handleResponseExceptionWithRequest { cause, _ ->
            throw cause.asApiException() as Throwable
        }
    }
}

expect fun createHttpClient(): HttpClient
