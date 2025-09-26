package org.com.bayarair.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.com.bayarair.data.token.TokenHandler

private const val PREFS_NAME = "auth_prefs"
private const val KEY_TOKEN = "token"

class AndroidTokenStore(
    context: Context,
) : TokenHandler {
    private val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    @Volatile private var memToken: String? = null

    @Volatile private var version: Int = 0

    init {
        memToken = prefs.getString(KEY_TOKEN, null)
        version = if (memToken == null) 0 else 1
    }

    override fun peekToken(): String? = memToken

    override fun sessionVersion(): Int = version

    override suspend fun getToken(): String? =
        memToken ?: withContext(Dispatchers.IO) {
            prefs.getString(KEY_TOKEN, null)?.also { memToken = it }
        }

    override suspend fun setToken(token: String) {
        memToken = token
        version += 1
        withContext(Dispatchers.IO) {
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }
    }

    override suspend fun clear() {
        memToken = null
        version += 1
        withContext(Dispatchers.IO) {
            prefs.edit().remove(KEY_TOKEN).apply()
        }
    }
}
