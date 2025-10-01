package org.com.bayarair.data.local

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.core.content.edit
import org.com.bayarair.data.token.TokenStorage

private const val PREFS_NAME = "auth_prefs"
private const val KEY_TOKEN = "token"

class AndroidTokenStorage(
    context: Context,
) : TokenStorage {
    private val prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    override fun readToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun writeToken(token: String?) {
        prefs.edit {
            if (token == null) {
                remove(KEY_TOKEN)
            } else {
                putString(KEY_TOKEN, token)
            }
        }
    }
}
