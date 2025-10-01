package org.com.bayarair.platform
// iosMain/kotlin/.../Network.kt
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import org.com.bayarair.core.AppEvent
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.token.TokenHandler
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object IosDI : KoinComponent {
    val tokenHandler by inject<TokenHandler>()
    val appEvents by inject<AppEvents>()
}

actual fun createHttpClient(): HttpClient {
    val tokenHandler = IosDI.tokenHandler
    val appEvents = IosDI.appEvents

    val base = HttpClient(Darwin) {
        engine {
            configureRequest { setAllowsCellularAccess(true) }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println("[Ktor] $message")
            }
            level = LogLevel.NONE
        }
    }

    return installCommonPlugins(
        client = base,
        fastTokenProvider = { tokenHandler.peekToken() },
        sessionVersionProvider = { tokenHandler.sessionVersion() },
        onUnauthorized = {
            tokenHandler.clear()
            appEvents.emit(
                AppEvent.Logout("Anda telah login di perangkat lain. Silakan login ulang")
            )
        }
    )
}
