package org.com.bayarair.platform

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun createHttpClient(): HttpClient

fun installCommonPlugins(client: HttpClient) =
    client.config {
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
    }
