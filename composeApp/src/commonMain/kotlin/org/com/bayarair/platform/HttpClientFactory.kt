package org.com.bayarair.platform

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.statement.*
import io.ktor.http.*

typealias TokenProvider = suspend () -> String?

fun installCommonPlugins(
    client: HttpClient,
    tokenProvider: TokenProvider = { null },
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

    install(Auth) {
        bearer {
            loadTokens {
                val token = tokenProvider()
                token?.let { BearerTokens(it, it) }
            }

            sendWithoutRequest { request ->
                val path = "/" + request.url.pathSegments.joinToString("/")
                shouldAttach(path.lowercase())
            }
        }
    }

    HttpResponseValidator {
        validateResponse { response ->
            if (response.status == HttpStatusCode.Unauthorized) {
                CoroutineScope(Dispatchers.Default).launch {
                    onUnauthorized()
                }
            }
        }
    }
}

expect fun createHttpClient(): HttpClient
