package org.com.bayarair.platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) { }.let(::installCommonPlugins)
