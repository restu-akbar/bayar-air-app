package org.com.bayarair.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.token.TokenHandler
import org.koin.java.KoinJavaComponent.getKoin

actual fun createHttpClient(): HttpClient {
    val tokenHandler: TokenHandler = getKoin().get()
    val appEvents: AppEvents = getKoin().get()

    return installCommonPlugins(
        client = HttpClient(OkHttp),
        tokenProvider = { tokenHandler.getToken() },
        onUnauthorized = {
            tokenHandler.clear()
            appEvents.emit(AppEvent.Logout)
        },
    )
}
