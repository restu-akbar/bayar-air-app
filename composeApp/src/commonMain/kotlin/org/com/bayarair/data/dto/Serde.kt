package org.com.bayarair.data.dto

import kotlinx.serialization.json.Json

object Serde {
    val relaxed: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
            isLenient = true
        }
    }
}
