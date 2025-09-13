package org.com.bayarair.platform

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

// suspend supaya bisa ambil token dari DataStore/Room, dll.
typealias TokenProvider = suspend () -> String?

fun installCommonPlugins(
    client: HttpClient,
    tokenProvider: TokenProvider = { null },
    shouldAttach: (String) -> Boolean = { path -> !path.contains("/login") },
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
}

// Tetap biarkan signature expect tanpa argumen
expect fun createHttpClient(): HttpClient
