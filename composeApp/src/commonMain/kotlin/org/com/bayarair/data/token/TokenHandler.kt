package org.com.bayarair.data.token

interface TokenHandler {
    suspend fun getToken(): String?

    suspend fun setToken(token: String)

    suspend fun clear()
}
