package org.com.bayarair.data.token

import android.content.Context
import android.content.Context.MODE_PRIVATE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "auth_prefs"
private const val KEY_TOKEN = "token"

class AndroidTokenStore(
    context: Context,
) : TokenHandler {
    private val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    override suspend fun getToken(): String? =
        withContext(Dispatchers.IO) {
            prefs.getString(KEY_TOKEN, null)
        }

    override suspend fun setToken(token: String) =
        withContext(Dispatchers.IO) {
            prefs.edit().putString(KEY_TOKEN, token).apply()
        }

    override suspend fun clear() =
        withContext(Dispatchers.IO) {
            prefs.edit().remove(KEY_TOKEN).apply()
        }
}
