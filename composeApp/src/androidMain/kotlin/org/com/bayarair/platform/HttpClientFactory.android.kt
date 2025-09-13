package org.com.bayarair.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.com.bayarair.data.token.TokenHandler
import org.koin.java.KoinJavaComponent.getKoin

actual fun createHttpClient(): HttpClient {
    val base =
        HttpClient(OkHttp) {
        }

    val tokenHandler: TokenHandler = getKoin().get()
    return installCommonPlugins(base, tokenProvider = { tokenHandler.getToken() })
}
