package org.com.bayarair.data.token

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.concurrent.Volatile

class DefaultTokenHandler(
    private val storage: TokenStorage,
) : TokenHandler {
    @Volatile
    private var memToken: String? = null

    @Volatile
    private var version: Int = 0

    init {
        memToken = storage.readToken()
        version = if (memToken == null) 0 else 1
    }

    override fun peekToken(): String? = memToken

    override fun sessionVersion(): Int = version

    override suspend fun getToken(): String? =
        memToken ?: withContext(Dispatchers.IO) {
            storage.readToken()?.also { memToken = it }
        }

    override suspend fun setToken(token: String) {
        memToken = token
        version += 1
        withContext(Dispatchers.IO) {
            storage.writeToken(token)
        }
    }

    override suspend fun clear() {
        memToken = null
        version += 1
        withContext(Dispatchers.IO) {
            storage.writeToken(null)
        }
    }
}

