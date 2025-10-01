package org.com.bayarair.data.token

interface TokenStorage {
    fun readToken(): String?

    fun writeToken(token: String?)
}
